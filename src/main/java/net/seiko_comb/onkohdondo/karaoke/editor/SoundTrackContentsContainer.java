package net.seiko_comb.onkohdondo.karaoke.editor;

import net.seiko_comb.onkohdondo.karaoke.data.SoundFile;
import net.seiko_comb.onkohdondo.karaoke.editor.data.SoundTrack;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class SoundTrackContentsContainer extends TrackContentsContainer {

	private SoundTrack track;
	private SoundFile soundFile;

	private double selectedTimeStart;
	private double selectedTimeEnd;

	public SoundTrackContentsContainer(MainPApplet p, SoundTrack track) {
		super(p);
		this.track = track;
		this.soundFile = track.getSoundFile();
	}

	@Override
	protected void draw() {
		backgroundArea(p.color(0));
		clipArea();
		float channelHeight = height / soundFile.getChannelCount();
		for (int i = 0; i < soundFile.getChannelCount(); i++) {
			p.pushMatrix();
			p.translate(0, i * channelHeight);

			p.strokeWeight(1);
			p.stroke(0, 255, 0);
			for (int j = 0; j < width; j++) {
				double tLow = trackContainer.getTimelineContainer()
						.getTimeOnPixelX(j);
				double tHigh = trackContainer.getTimelineContainer()
						.getTimeOnPixelX(j + 1);
				float min = (float) track.getChannel(i).min(tLow, tHigh);
				float max = (float) track.getChannel(i).max(tLow, tHigh);
				float minY = PApplet.map(min, 1, -1, 0, channelHeight);
				float maxY = PApplet.map(max, 1, -1, 0, channelHeight);
				p.line(j, minY, j, maxY);
			}

			p.popMatrix();
		}

		if (mouseIsIn()) {
			float x = mouseX;
			p.strokeWeight(1);
			p.stroke(128);
			p.line(x, 0, x, height);
		}

		float xStart = trackContainer.getTimelineContainer()
				.getPixelXOnTime(selectedTimeStart);
		float xEnd = trackContainer.getTimelineContainer()
				.getPixelXOnTime(selectedTimeEnd);
		p.strokeWeight(1);
		p.stroke(255);
		p.line(xStart, 0, xStart, height);
		p.line(xEnd, 0, xEnd, height);
		p.noStroke();
		p.fill(0, 0, 255, 64);
		p.rect(xStart, 0, xEnd - xStart, height);

		p.stroke(0, 204, 0);
		p.getEditorManager().getPlayingSecond()
				.map(trackContainer.getTimelineContainer()::getPixelXOnTime)
				.ifPresent(x -> p.line(x, 0, x, height));
	}

	@Override
	public boolean mousePressed(MouseEvent event, float mouseX, float mouseY) {
		if (super.mousePressed(event, mouseX, mouseY)) return true;
		double time = trackContainer.getTimelineContainer()
				.getTimeOnPixelX(mouseX);
		selectedTimeStart = selectedTimeEnd = time;
		return true;
	}

	@Override
	public boolean mouseDragged(MouseEvent event, float mouseX, float mouseY) {
		if (super.mouseDragged(event, mouseX, mouseY)) return true;
		double time = trackContainer.getTimelineContainer()
				.getTimeOnPixelX(mouseX);
		selectedTimeEnd = time;
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) {
			return true;
		}
		if (event.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
			if (p.getEditorManager().playing()) {
				p.getEditorManager().stopSound();
			} else {
				if (selectedTimeStart == selectedTimeEnd) {
					p.getEditorManager().playSound(selectedTimeStart);
				} else {
					double start = Math.min(selectedTimeStart, selectedTimeEnd);
					double end = Math.max(selectedTimeStart, selectedTimeEnd);
					p.getEditorManager().playSound(start, end);
				}
			}
			return true;
		}
		return false;
	}
}
