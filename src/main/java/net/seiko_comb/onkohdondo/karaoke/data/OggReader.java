package net.seiko_comb.onkohdondo.karaoke.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

public class OggReader {
	private Path filePath;

	public OggReader(Path filePath) {
		this.filePath = filePath;
	}

	private final int BUFSIZE = 4096 * 2;
	private int convsize = BUFSIZE * 2;
	private byte[] convbuffer = new byte[convsize];
	private SyncState oSyncState;
	private StreamState oStreamState;
	private Page oPage;
	private Packet oPacket;
	private Info vInfo;
	private Comment vComment;
	private DspState vDspState;
	private Block vBlock;
	private ByteArrayOutputStream outputLine;
	// private SourceDataLine outputLine;
	private BufferedInputStream bitStream = null;

	private byte[] buffer = null;
	private int bytes = 0;

	private byte[] result;

	public void read() throws IOException {
		boolean chained = false;

		init(Files.newInputStream(filePath));
		initJOrbis();

		while (true) {
			int eos = 0;

			int index = oSyncState.buffer(BUFSIZE);
			buffer = oSyncState.data;
			bytes = bitStream.read(buffer, index, BUFSIZE);
			oSyncState.wrote(bytes);

			if (chained) {
				chained = false;
			} else {
				if (oSyncState.pageout(oPage) != 1) {
					if (bytes < BUFSIZE) break;
					throw new IllegalStateException(
							"Input does not appear to be an Ogg bitstream.");
				}
			}
			oStreamState.init(oPage.serialno());
			oStreamState.reset();

			vInfo.init();
			vComment.init();

			if (oStreamState.pagein(oPage) < 0) {
				// error; stream version mismatch perhaps
				throw new IllegalStateException(
						"Error reading first page of Ogg bitstream data.");
			}

			if (oStreamState.packetout(oPacket) != 1) {
				// no page? must not be vorbis
				throw new IllegalStateException(
						"Error reading initial header packet.");
			}

			if (vInfo.synthesis_headerin(vComment, oPacket) < 0) {
				// error case; not a vorbis header
				throw new IllegalStateException(
						"This Ogg bitstream does not contain Vorbis audio data.");
			}

			int i = 0;
			while (i < 2) {
				while (i < 2) {
					int result = oSyncState.pageout(oPage);
					if (result == 0) break; // Need more data
					if (result == 1) {
						oStreamState.pagein(oPage);
						while (i < 2) {
							result = oStreamState.packetout(oPacket);
							if (result == 0) break;
							if (result == -1) {
								throw new IllegalStateException(
										"Corrupt secondary header.  Exiting.");
							}
							vInfo.synthesis_headerin(vComment, oPacket);
							i++;
						}
					}
				}

				index = oSyncState.buffer(BUFSIZE);
				buffer = oSyncState.data;
				bytes = bitStream.read(buffer, index, BUFSIZE);
				if (bytes == 0 && i < 2) {
					throw new IllegalStateException(
							"End of file before finding all Vorbis headers!");
				}
				oSyncState.wrote(bytes);
			}

			convsize = BUFSIZE / vInfo.channels;

			vDspState.synthesis_init(vInfo);
			vBlock.init(vDspState);

			float[][][] _pcmf = new float[1][][];
			int[] _index = new int[vInfo.channels];

			initOutputBuffer(vInfo.channels, vInfo.rate);

			while (eos == 0) {
				while (eos == 0) {
					int result = oSyncState.pageout(oPage);
					if (result == 0) break; // need more data
					if (result == -1) {
						// missing or corrupt data at this page position
						// System.err
						// .println("Corrupt or missing data in bitstream;"
						// + "continuing...");
					} else {
						oStreamState.pagein(oPage);

						if (oPage.granulepos() == 0) { //
							chained = true; //
							eos = 1; //
							break; //
						} //

						while (true) {
							result = oStreamState.packetout(oPacket);
							if (result == 0) break; // need more data
							if (result == -1) { // missing or corrupt data at
								// this page position
								// no reason to complain; already complained
								// above

								// System.err.println("no reason to complain;
								// already complained above");
							} else {
								// we have a packet. Decode it
								int samples;
								if (vBlock.synthesis(oPacket) == 0) {
									// test for success!
									vDspState.synthesis_blockin(vBlock);
								}
								while ((samples = vDspState
										.synthesis_pcmout(_pcmf, _index)) > 0) {
									float[][] pcmf = _pcmf[0];
									int bout = (samples < convsize ? samples
											: convsize);

									// convert doubles to 16 bit signed ints
									// (host order) and
									// interleave
									for (i = 0; i < vInfo.channels; i++) {
										int ptr = i * 2;
										// int ptr=i;
										int mono = _index[i];
										for (int j = 0; j < bout; j++) {
											int val = (int) (pcmf[i][mono + j]
													* 32767.);
											if (val > 32767) {
												val = 32767;
											}
											if (val < -32768) {
												val = -32768;
											}
											if (val < 0) val = val | 0x8000;
											convbuffer[ptr] = (byte) (val);
											convbuffer[ptr
													+ 1] = (byte) (val >>> 8);
											ptr += 2 * (vInfo.channels);
										}
									}
									outputLine.write(convbuffer, 0,
											2 * vInfo.channels * bout);
									vDspState.synthesis_read(bout);
								}
							}
						}
						if (oPage.eos() != 0) eos = 1;
					}
				}

				if (eos == 0) {
					index = oSyncState.buffer(BUFSIZE);
					buffer = oSyncState.data;
					bytes = bitStream.read(buffer, index, BUFSIZE);
					if (bytes == -1) {
						break;
					}
					oSyncState.wrote(bytes);
					if (bytes == 0) eos = 1;
				}
			}

			oStreamState.clear();
			vBlock.clear();
			vDspState.clear();
			vInfo.clear();
		}

		result = outputLine.toByteArray();
	}

	private void init(InputStream in) throws IOException {
		if (in == null) {
			throw new IOException("Couldn't find input source");
		}
		bitStream = new BufferedInputStream(in);
		bitStream.mark(Integer.MAX_VALUE);
	}

	private void initJOrbis() {
		oSyncState = new SyncState();
		oStreamState = new StreamState();
		oPage = new Page();
		oPacket = new Packet();

		vInfo = new Info();
		vComment = new Comment();
		vDspState = new DspState();
		vBlock = new Block(vDspState);

		buffer = null;
		bytes = 0;

		oSyncState.init();
	}

	private void initOutputBuffer(int channels, int rate) {
		outputLine = new ByteArrayOutputStream();
		// AudioFormat audioFormat = new AudioFormat(rate, 16, channels, true,
		// // PCM_Signed
		// false // littleEndian
		// );
		// DataLine.Info info = new DataLine.Info(SourceDataLine.class,
		// audioFormat, AudioSystem.NOT_SPECIFIED);
		// if (!AudioSystem.isLineSupported(info)) {
		// }
		//
		// try {
		// outputLine = (SourceDataLine) AudioSystem.getLine(info);
		// // outputLine.addLineListener(this);
		// outputLine.open(audioFormat);
		// outputLine.start();
		// } catch (LineUnavailableException e) {
		// e.printStackTrace();
		// }
	}

	public byte[] getResult() {
		return result;
	}

	public Info getvInfo() {
		return vInfo;
	}
}
