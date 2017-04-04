package net.seiko_comb.onkohdondo.karaoke.editor;

import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_M;
import static java.awt.event.KeyEvent.VK_T;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.seiko_comb.onkohdondo.karaoke.editor.data.Project;
import net.seiko_comb.onkohdondo.karaoke.editor.data.Track;
import net.seiko_comb.onkohdondo.uihelper.Container;
import net.seiko_comb.onkohdondo.uihelper.ScrollBarContainer;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class TimelineContainer extends Container<MainPApplet> {

	public TimelineContainer(MainPApplet p) {
		super(p);
		verticalScrollBar = new ScrollBarContainer(p, this, false);
		horizontalScrollBar = new ScrollBarContainer(p, this, true);
		horizontalScrollBar.setFullValue(0,
				getProject().getFullTimeDuration() + 5);
		horizontalScrollBar.setDisplayValue(0, 10);
		timeScaleContainer = new TimeScaleContainer(p);
	}

	private float timeScaleHeight = 50;
	private ScrollBarContainer horizontalScrollBar, verticalScrollBar;
	private float timelineContainerLabelWidth = 100;
	private Map<Track, TrackContainer> trackContainerMap = new HashMap<>();
	private TimeScaleContainer timeScaleContainer;

	private float drawY;

	@Override
	public void draw() {
		horizontalScrollBar.setFullValue(0,
				getProject().getFullTimeDuration() + 5);
		backgroundArea(p.color(204));
		clipArea();

		float initDrawY = timeScaleHeight
				- verticalScrollBar.getDisplayLow().floatValue();
		drawY = initDrawY;
		p.getEditorManager().getProject().getTracks().forEach(track -> {
			TrackContainer trackContainer = getTrackContainer(track);
			p.clip(0, timeScaleHeight, width - verticalScrollBar.getSize(),
					height - horizontalScrollBar.getSize() - timeScaleHeight);
			trackContainer.redraw(0, drawY,
					width - horizontalScrollBar.getSize(), 0);
			drawY += trackContainer.getHeight() + 3;
		});
		p.noClip();
		verticalScrollBar.setFullValue(0, drawY - initDrawY);
		verticalScrollBar.setDisplayValueDiff(
				height - timeScaleHeight - horizontalScrollBar.getSize());

		timeScaleContainer.redraw(0, 0, width, timeScaleHeight);

		verticalScrollBar.redrawToDefaultPlace(timeScaleHeight,
				height - horizontalScrollBar.getSize() - timeScaleHeight);
		horizontalScrollBar.redrawToDefaultPlace(timelineContainerLabelWidth,
				width - verticalScrollBar.getSize()
						- timelineContainerLabelWidth);
	}

	private TrackContainer getTrackContainer(Track track) {
		TrackContainer ret = trackContainerMap.get(track);
		if (ret == null) {
			ret = TrackContainer.getTrackContainer(p, this, track);
			trackContainerMap.put(track, ret);
		}
		return ret;
	}

	@Override
	public void forEachChildContainer(Consumer<Container<?>> c) {
		c.accept(horizontalScrollBar);
		c.accept(verticalScrollBar);
		try {
			p.getEditorManager().getProject().getTracks().stream()
					.map(trackContainerMap::get).filter(a -> a != null)
					.forEach(c);
		} catch (ConcurrentModificationException e) {
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!super.keyPressed(event)) {
			if (event.getKeyCode() == VK_I) {
				if ((event.getModifiers() & (Event.CTRL | Event.SHIFT)) > 0) {
					p.getEditorManager().addAudioTrackFromFile();
					return true;
				}
			} else if (event.getKeyCode() == VK_T) {
				if ((event.getModifiers() & (Event.CTRL)) > 0) {
					p.getEditorManager().addTempoTrack();
					return true;
				}
			} else if (event.getKeyCode() == VK_M) {
				if ((event.getModifiers() & (Event.CTRL)) > 0) {
					p.getEditorManager().addMelodyTrack();
					return true;
				}
			}
		}
		return false;
	}

	public float getTimelineContainerLabelWidth() {
		return timelineContainerLabelWidth;
	}

	public Project getProject() {
		return p.getEditorManager().getProject();
	}

	public double getTimeOnPixelX(float pixelX) {
		double w = width - timelineContainerLabelWidth
				- verticalScrollBar.getSize();
		double low = horizontalScrollBar.getDisplayLow().doubleValue();
		double high = horizontalScrollBar.getDisplayHigh().doubleValue();
		double ret = low + (pixelX / w) * (high - low);
		return ret;
	}

	public float getPixelXOnTime(double time) {
		double w = width - timelineContainerLabelWidth
				- verticalScrollBar.getSize();
		double low = horizontalScrollBar.getDisplayLow().doubleValue();
		double high = horizontalScrollBar.getDisplayHigh().doubleValue();
		double ret = (time - low) / (high - low) * w;
		return (float) ret;
	}

	public ScrollBarContainer getHorizontalScrollBar() {
		return horizontalScrollBar;
	}

	@Override
	public boolean mouseWheel(MouseEvent event, float mouseX, float mouseY) {
		if (!super.mouseWheel(event, mouseX, mouseY)) {
			if (event.getModifiers() == 0) {
				verticalScrollBar
						.setDisplayValue(d -> d + event.getCount() * 7);
			}
			return false;
		}
		return true;
	}

	private class TimeScaleContainer extends Container<MainPApplet> {
		public TimeScaleContainer(MainPApplet p) {
			super(p);
		}

		@Override
		protected void draw() {
			clipArea();
			backgroundArea(p.color(216));

			p.stroke(0);
			p.strokeWeight(2);
			p.line(0, height, width, height);

			double durationMin = getTimeOnPixelX(40) - getTimeOnPixelX(0);
			int digit = (int) Math.ceil(Math.log10(durationMin));
			double duration = Math.pow(10, digit);
			double leftTime = getTimeOnPixelX(-timelineContainerLabelWidth);
			double rightTime = getTimeOnPixelX(
					width - timelineContainerLabelWidth);
			for (double t = duration * (int) (leftTime / duration)
					- duration; t < rightTime; t += duration) {
				float x = timelineContainerLabelWidth + getPixelXOnTime(t);
				p.stroke(0);
				p.strokeWeight(1);
				p.line(x, height, x, height - 12);

				String tStr = String.format("%." + Math.max(1, -digit) + "f",
						t);
				p.fill(0);
				p.textAlign(CENTER);
				p.textFont(p.getUiGothic(), 12);
				p.text(tStr, x, height - 15);
				for (int i = 1; i < 10; i++) {
					double tt = t + duration / 10 * i;
					float xx = timelineContainerLabelWidth
							+ getPixelXOnTime(tt);
					p.line(xx, height, xx, height - 5);
				}
			}
		}
	}
}
