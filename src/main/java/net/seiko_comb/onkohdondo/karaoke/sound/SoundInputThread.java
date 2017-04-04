package net.seiko_comb.onkohdondo.karaoke.sound;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class SoundInputThread implements Runnable {

	public final double LOG_2 = Math.log(2) / 12.0;

	public static void main(String[] args) {
		try {
			new SoundInputThread(10, 512);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	private boolean loop = true;
	private AudioFormat format;
	private TargetDataLine line;
	private AudioInputStream in;

	private byte[] buffer;
	public int[] left, right, center;
	private int size;
	private FFT fft;

	public int sampleRate = 44100;
	public int analyzeBufferSize;
	public double[] frequencyBuffer, clearityBuffer, volumeBuffer;

	private long startMillis;

	public SoundInputThread(int size, int analyzeBufferSize)
			throws LineUnavailableException {
		this(size, analyzeBufferSize, true);
	}

	private void initSoundInput() throws LineUnavailableException {
		format = new AudioFormat(sampleRate, 16, 2, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		line = (TargetDataLine) AudioSystem.getLine(info);
		line.open(format);
		line.start();
		startMillis = System.currentTimeMillis();
		in = new AudioInputStream(line);
	}

	public SoundInputThread(int size, int analyzeBufferSize, boolean soundInput)
			throws LineUnavailableException {
		initMemory(size, analyzeBufferSize);
		if (soundInput) {
			initSoundInput();
			new Thread(this).start();
		}
	}

	private void initMemory(int size, int analyzeBufferSize) {
		this.analyzeBufferSize = analyzeBufferSize;
		analyzeFrame = analyzeBufferSize;
		this.size = 1 << size;
		buffer = new byte[this.size * 4];
		left = new int[this.size];
		right = new int[this.size];
		center = new int[this.size];
		fft = new FFT(size);

		frequencyBuffer = new double[analyzeBufferSize];
		clearityBuffer = new double[analyzeBufferSize];
		volumeBuffer = new double[analyzeBufferSize];
	}

	@Override
	public void run() {
		try {
			// System.out.println(startMillis);
			while (loop) {
				loop();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		close();
	}

	private int analyzeFrame;

	// private int count = 0;
	private void loop() throws IOException {
		in.read(buffer, 0, size * 4);
		analyze();
	}

	private int bufferPointer = 0;

	public void write(byte[] inputBuffer, int inputSize) {
		int bufferSize = size * 4;
		int inputPointer = 0;
		while (inputPointer < inputSize) {
			int length = Math.min(inputSize - inputPointer,
					bufferSize - bufferPointer);
			System.arraycopy(inputBuffer, inputPointer, buffer, bufferPointer,
					length);
			inputPointer += length;
			bufferPointer += length;
			if (bufferPointer == bufferSize) {
				analyze();
				bufferPointer = 0;
			}
		}
	}

	public void analyze() {
		fft.clear();
		for (int i = 0; i < size; i++) {
			left[i] = (buffer[i * 4 + 0] & 0xff) | (buffer[i * 4 + 1] << 8);
			right[i] = (buffer[i * 4 + 2] & 0xff) | (buffer[i * 4 + 3] << 8);
			center[i] = left[i] + right[i];
			fft.f[i] = center[i];
		}
		fft.analyze();
		double note = Math.log(fft.frequency / 27.5) / LOG_2 + 21;
		int frame = analyzeFrame % analyzeBufferSize;
		frequencyBuffer[frame] = note;
		clearityBuffer[frame] = fft.clearity;
		volumeBuffer[frame] = fft.volume;
		analyzeFrame++;
	}

	private double volumeThreshold = 65536 * 0.05;
	private double clearityThreshold = 0.9;

	public Optional<Double> nearest(double pitch, long start, long end) {
		int count = 0;
		double minDelta = 10000000;
		for (int i = getFrame(start); i < getFrame(end); i++) {
			int j = i % analyzeBufferSize;
			if (volumeBuffer[j] > volumeThreshold
					&& clearityBuffer[j] > clearityThreshold) {
				double delta = frequencyBuffer[j] - pitch;
				delta += 6;
				delta = (delta % 12 + 12) % 12;
				delta -= 6;
				if (Math.abs(delta) < Math.abs(minDelta)) minDelta = delta;
				count++;
			}
		}
		if (count >= 1)
			return Optional.of(pitch + minDelta);
		else return Optional.empty();
	}

	public Optional<Double> median(double pitch, long start, long end) {
		double[] list = IntStream.range(getFrame(start), getFrame(end))
				.map(i -> i % analyzeBufferSize)
				.filter(i -> volumeBuffer[i] > volumeThreshold)
				.filter(i -> clearityBuffer[i] > clearityThreshold)
				.mapToDouble(i -> frequencyBuffer[i] - pitch)
				.map(delta -> delta + 6).map(delta -> (delta % 12 + 12) % 12)
				.map(delta -> delta - 6).map(delta -> delta + pitch).sorted()
				.toArray();
		int size = list.length;
		if (size >= 3)
			return Optional.of((list[size / 2] + list[(size + 1) / 2]) / 2);
		else return Optional.empty();
	}

	public int getFrame(long millis) {
		return (int) ((millis - startMillis) / (1000.0 / sampleRate * size))
				+ analyzeBufferSize;
	}

	public boolean hasData(long millis) {
		return getFrame(millis) < analyzeFrame;
	}

	public void loopEnd() {
		loop = false;
	}

	public void close() {
		line.close();
		line.stop();
	}

	public int getBufferSize() {
		return size;
	}

	public FFT getFFT() {
		return fft;
	}

	public int getSize() {
		return size;
	}

}
