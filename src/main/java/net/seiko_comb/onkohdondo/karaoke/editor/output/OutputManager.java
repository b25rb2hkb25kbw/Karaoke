package net.seiko_comb.onkohdondo.karaoke.editor.output;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class OutputManager {

	public OutputManager() {
		Thread thread = new Thread(this::run);
		thread.setDaemon(true);
		thread.start();

		buffer = new byte[1024];
	}

	public void run() {
		try {
			setup();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			end();
			return;
		}
		run = true;
		while (run) {
			loop();
		}
	}

	private AudioFormat format;
	private DataLine.Info info;
	private SourceDataLine source;

	private ArrayList<OutputControlerStatus> wrappers = new ArrayList<>();
	private byte[] buffer;

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

	private boolean run = true;

	public void end() {
		run = false;
		closeSource(source);
	}

	private void closeSource(SourceDataLine source) {
		if (source != null) {
			source.stop();
			source.close();
		}
	}

	private void loop() {
		while (source.getBufferSize() - source.available() > buffer.length * 10)
			;
		int length = buffer.length / 4;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < 2; j++) {
				double val = 0;
				for (OutputControlerStatus wrapper : wrappers) {
					val += wrapper.controler.getOutput(j,
							wrapper.time + i * (1.0 / 44100.0));
				}
				int intVal = (int) (val * 32768);
				buffer[i * 4 + j * 2 + 0] = (byte) (intVal >> 0 & 0xff);
				buffer[i * 4 + j * 2 + 1] = (byte) (intVal >> 8 & 0xff);
			}
		}
		for (ListIterator<OutputControlerStatus> it = wrappers
				.listIterator(); it.hasNext();) {
			OutputControlerStatus wrapper = it.next();
			if (wrapper.controler.end()) {
				it.remove();
				continue;
			}
			wrapper.time += length * (1.0 / 44100.0);
		}
		source.write(buffer, 0, buffer.length);
	}

	public OutputControlerStatus addOutputControler(OutputControler out) {
		OutputControlerStatus wrapper = new OutputControlerStatus(out, System.currentTimeMillis());
		wrappers.add(
				wrapper);
		return wrapper;
	}

}
