package net.seiko_comb.onkohdondo.karaoke.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.newdawn.easyogg.OggClip;

public class OutputManager {
	public OutputManager() {
		Thread thread = new Thread(this::run);
		thread.setDaemon(true);
		thread.start();
	}

	public void run() {
		try {
			setup();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			end();
			return;
		}
		while (run) {
			loop();
		}
	}

	private AudioFormat format;
	private DataLine.Info info;
	private SourceDataLine source;

	private void setup() throws LineUnavailableException {
		source = createSource();
	}

	private SourceDataLine createSource() throws LineUnavailableException {
		format = new AudioFormat(44100, 16, 2, true, false);
		info = new DataLine.Info(SourceDataLine.class, format);
		SourceDataLine source = (SourceDataLine) AudioSystem.getLine(info);
		source.open(format);
		source.start();
		return source;
	}

	private void loop() {
		try {
			// Arrays.fill(bufferVal, 0);
			// runSE(donList, don);
			// for (int i = 0; i < BUFFER_SIZE / 2; i++) {
			// buffer[i * 2 + 0] = (byte) (bufferVal[i] >> 0 & 0xff);
			// buffer[i * 2 + 1] = (byte) (bufferVal[i] >> 8 & 0xff);
			// }
			// source.write(buffer, 0, buffer.length);
		} catch (Exception e) {
		}
	}

	private void closeSource(SourceDataLine source) {
		if (source != null) {
			source.stop();
			source.close();
		}
	}

	private boolean run = true;

	public void end() {
		run = false;
		closeSource(source);
	}

	private Optional<OggClip> playingClip = Optional.empty();

	public void playSong(File file, long start, double gain) {
		playSong(file, start, gain, null);
	}

	public void playSong(File file, long start, double gain,
			SoundInputThread input) {
		new Thread(() -> {
			synchronized (playingClip) {
				// stopSong();
				try {
					InputStream in = new BufferedInputStream(
							new FileInputStream(file));
					OggClip clip = new OggClip(in);
					clip.setGain((float) Math.log10(gain) * 20);
					playingClip = Optional.of(clip);
					clip.analyzer = Optional.ofNullable(input);
					clip.play(start);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void stopSong() {
		playingClip.ifPresent(OggClip::stop);
	}

	public boolean playingSong() {
		return playingClip.map(OggClip::playing).orElse(false);
	}

	public Optional<OggClip> getPlayingClip() {
		return playingClip;
	}

}
