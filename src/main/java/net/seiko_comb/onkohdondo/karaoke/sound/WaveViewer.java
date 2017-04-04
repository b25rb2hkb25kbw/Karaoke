package net.seiko_comb.onkohdondo.karaoke.sound;

import java.io.File;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import javax.sound.sampled.LineUnavailableException;

import processing.core.PApplet;

public class WaveViewer extends PApplet {

	public static void main(String[] args) {
		PApplet.main(WaveViewer.class.getName());
	}

	// private Minim minim;
	// private AudioInput audioInput;

	private SoundInputThread in;

	public void settings() {
		// size(1024, 600);
		fullScreen();
	}

	@SuppressWarnings("unused")
	private OutputManager out;

	@Override
	public void setup() {
		try {
			in = new SoundInputThread(9, 512, true);
			out = new OutputManager();
			String file;
			file = "C:/Users/ksam3/Videos/kiniroginiromosaic.ogg";
//			out.playSong(new File(file), System.currentTimeMillis(), 1.0, in);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		textSize(50);
	}

	@Override
	public void draw() {
		background(0);
		drawWave(0, 100);
		stroke(32);
		line(0, 100, width, 100);

		noStroke();

		int size = in.analyzeBufferSize;
		int low = 48, high = 64;
		IntFunction<Float> frameToX = i -> map(i % size, 0, size, 0, width);
		DoubleFunction<Float> pitchToY = pitch -> constrain(
				map((float) pitch, low, high, height - 100, 100), 100,
				height - 100);

		stroke(255, 0, 0);
		IntStream.rangeClosed(low, high).mapToDouble(a -> a).mapToObj(pitchToY)
				.forEach(y -> {
					line(0, y, width, y);
				});

		noStroke();
		for (int i = 0; i < size; i++) {
			if (in.volumeBuffer[i] > 65536 * 0.0
					&& in.clearityBuffer[i] > 0.0) {
				float clearity = map((float) in.clearityBuffer[i], 0, 1, 210,
						40);
				colorMode(HSB);
				fill(clearity, 255, 255);
				colorMode(RGB);
				float frequencyY = pitchToY.apply(in.frequencyBuffer[i]);
				ellipse(frameToX.apply(i), frequencyY, 2, 2);
				fill(0, 0, 255);
				ellipse(frameToX.apply(i), map((float) in.clearityBuffer[i], 0,
						1, height - 50, height - 100), 2, 2);
				fill(255, 0, 255);
				ellipse(frameToX.apply(i), map((float) in.volumeBuffer[i], 0,
						65536, height, height - 50), 2, 2);
			}
		}

		// stroke(255, 204, 0);
		// float nowX = frameToX.apply(in.getFrame(System.currentTimeMillis()));
		// line(nowX, 100, nowX, 400);
		//
		// for (long millis = System.currentTimeMillis() / 1000 * 1000
		// - 1000, i = 0; i < 4; i++, millis += 1000) {
		// float x = frameToX.apply(in.getFrame(millis));
		// line(x, 100, x, 400);
		// }

		if (pressStart != -1) {
			stroke(0, 0, 255);
			float x = frameToX.apply(in.getFrame(pressStart));
			line(x, 100, x, 400);
			stroke(0, 0, 255);
			in.nearest(55, pressStart, pressEnd - 300)
					.map(i -> Math.round(i * 8) / 8.0).map(pitchToY::apply)
					.ifPresent(y -> {
						System.out.println(y);
						line(0, y, width, y);
					});
		}
	}

	private void drawWave(float y, float h) {
		stroke(0, 255, 0);
		for (int i = 0; i < in.getBufferSize(); i++) {
			float x = ((float) i) / in.getBufferSize() * width;
			point(x, map(in.center[i], -65536, 65534, y, y + h));
		}
	}

	private long pressStart = -1, pressEnd = -1;

	@Override
	public void keyPressed() {
		if (pressStart == -1) pressStart = System.currentTimeMillis();
		pressEnd = System.currentTimeMillis();
	}

	@Override
	public void keyReleased() {
		pressStart = pressEnd = -1;
	}

	@Override
	public void exit() {
		super.exit();
	}

}
