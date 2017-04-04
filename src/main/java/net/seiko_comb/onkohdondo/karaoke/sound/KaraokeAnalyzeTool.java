package net.seiko_comb.onkohdondo.karaoke.sound;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import net.seiko_comb.onkohdondo.karaoke.editor.output.FrequencyOutputControler;
import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputManager;
import net.seiko_comb.onkohdondo.uihelper.Container;
import net.seiko_comb.onkohdondo.uihelper.ContainerPApplet;
import net.seiko_comb.onkohdondo.util.ConcurrentLoopableQueue;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class KaraokeAnalyzeTool extends ContainerPApplet {
	public static void main(String[] args) {
		PApplet.main(KaraokeAnalyzeTool.class.getName());
	}

	@Override
	public void settings() {
		// size(640, 360);
		size(1280, 720);
	}

	private OutputManager out;
	private FrequencyOutputControler controler;
	private ConcurrentLoopableQueue<Record> recordList = new ConcurrentLoopableQueue<>(
			10000);

	private double pitch = 71;

	public void setup() {
		initOutput();
		initGUI();
	}

	private void initOutput() {
		out = new OutputManager();
		controler = new FrequencyOutputControler(
				x -> Math.sin(x * 2 * Math.PI));
		controler.setFrequency(getFrequencyFromPitch(pitch));
		controler.setVelocity(0.01);
		controler.setRecorder(0.01, (t, v, f) -> {
			recordList.addLast(new Record(t, v, f));
		});
		out.addOutputControler(controler);
	}

	public double getFrequencyFromPitch(double pitch) {
		return 440.0 * Math.pow(2, (pitch - 69) / 12);
	}

	private double LOG_2 = Math.log(2);

	public double getPitchFromFrequency(double frequency) {
		return Math.log(frequency / 440) / LOG_2 * 12 + 69;
	}

	private class Record {
		private double time, frequency, velocity;

		private Record(double time, double velocity, double frequency) {
			this.time = time;
			this.frequency = frequency;
			this.velocity = velocity;
		}
	}

	private BaseContainer baseContainer;

	private void initGUI() {
		baseContainer = new BaseContainer(this);
	}

	@Override
	protected Container<? extends ContainerPApplet> getTopLevelContainer() {
		return baseContainer;
	}

	private class BaseContainer extends Container<KaraokeAnalyzeTool> {
		private PitchViewer pitchViewer;
		private InfoBoardContainer infoBoardContainer;

		public BaseContainer(KaraokeAnalyzeTool p) {
			super(p);
			this.pitchViewer = new PitchViewer(p);
			this.infoBoardContainer = new InfoBoardContainer(p);
		}

		@Override
		protected void draw() {
			pitchViewer.redraw(0, 0, width, height * 2 / 3);
			infoBoardContainer.redraw(0, pitchViewer.getHeight(), width,
					height - pitchViewer.getHeight());
		}

		@Override
		public void forEachChildContainer(Consumer<Container<?>> c) {
			c.accept(pitchViewer);
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (super.keyTyped(event)) return true;
			if (event.getKeyCode() == java.awt.event.KeyEvent.VK_K) {
				pitch = (int) pitch;
				pitch++;
				controler.setFrequency(getFrequencyFromPitch(pitch));
				return true;
			} else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_J) {
				pitch = (int) pitch;
				pitch--;
				controler.setFrequency(getFrequencyFromPitch(pitch));
				return true;
			} else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_L) {
				pitch += 1.0 / 8;
				controler.setFrequency(getFrequencyFromPitch(pitch));
				return true;
			} else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_H) {
				pitch -= 1.0 / 8;
				controler.setFrequency(getFrequencyFromPitch(pitch));
				return true;
			} else if (event.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
				controler.setFrequency(t -> getFrequencyFromPitch(
						pitch + 0.5 * Math.sin(4 * t * 2 * Math.PI)));
				return true;
			}
			return false;
		}
	}

	private int[] whites = { 0, 2, 4, 5, 7, 9, 11 };

	private boolean isWhite(int index) {
		return IntStream.of(whites).anyMatch(i -> i == index % 12);
	}

	private class PitchViewer extends Container<KaraokeAnalyzeTool> {
		public PitchViewer(KaraokeAnalyzeTool p) {
			super(p);
		}

		@Override
		protected void draw() {
			clipArea();
			backgroundArea(0);
			float x = width - 30, lastX = x, lastY = 0;
			float low = 60, high = 84;
			for (int p = (int) low; p <= high; p++) {
				float y = map(p, high, low, 0, height);
				if (isWhite(p))
					stroke(192);
				else stroke(128);
				line(0, y, width, y);
			}
			for (ConcurrentLoopableQueue<Record>.Pointer it = recordList
					.end();;) {
				Optional<Record> recordOpt = it.backward();
				if (!recordOpt.isPresent()) break;
				if (x < 0) {
					it.removeFromHeadThroughHere();
					break;
				}
				Record record = recordOpt.get();
				float y = map((float) getPitchFromFrequency(record.frequency),
						high, low, 0, height);
				stroke(255, 204, 0, (float) ((1 - record.velocity) * 255));
				if (lastX != x) line(x, y, lastX, lastY);
				lastX = x;
				lastY = y;
				x--;
			}
		}
	}

	private class InfoBoardContainer extends Container<KaraokeAnalyzeTool> {
		public InfoBoardContainer(KaraokeAnalyzeTool p) {
			super(p);
		}

		@Override
		protected void draw() {
			backgroundArea(color(255, 255, 255));
			fill(0);
			textSize(50);
			text("Pitch:", 40, 90);
			text(pitch + "Hz", 40, 150);
		}
	}
}
