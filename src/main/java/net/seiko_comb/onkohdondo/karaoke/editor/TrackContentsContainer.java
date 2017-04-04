package net.seiko_comb.onkohdondo.karaoke.editor;

import net.seiko_comb.onkohdondo.uihelper.Container;
import net.seiko_comb.onkohdondo.uihelper.ScrollBarContainer;
import processing.event.MouseEvent;

public class TrackContentsContainer extends Container<MainPApplet> {

	protected TrackContainer trackContainer;

	public TrackContentsContainer(MainPApplet p) {
		super(p);
	}

	@Override
	protected void draw() {
		p.strokeWeight(0);
		p.stroke(0);
		p.fill(204, 204, 102);

		// System.out.println(width + "\t" + height);
		p.rect(0, 0, width - 1, height - 1);
	}

	protected void setTrackContainer(TrackContainer trackContainer) {
		this.trackContainer = trackContainer;
	}

	@Override
	public boolean mouseWheel(MouseEvent event, float mouseX, float mouseY) {
		if (!super.mouseWheel(event, mouseX, mouseY)) {
			ScrollBarContainer scrollBar = trackContainer.getTimelineContainer()
					.getHorizontalScrollBar();
			if ((event.getModifiers() & MouseEvent.SHIFT) > 0) {
				scrollBar.setDisplayValue(
						d -> d + event.getCount() * scrollBar.lowHigh() / 20);
				return true;
			} else if ((event.getModifiers() & MouseEvent.CTRL) > 0) {
				double beforeLow = scrollBar.getDisplayLow().doubleValue();
				double beforeHigh = scrollBar.getDisplayHigh().doubleValue();
				double beforeMouse = mouseX / width * (beforeHigh - beforeLow)
						+ beforeLow;
				double times = Math.pow(Math.sqrt(2), event.getCount());
				double afterLow = beforeMouse
						+ (beforeLow - beforeMouse) * times;
				double afterHigh = beforeMouse
						+ (beforeHigh - beforeMouse) * times;
				scrollBar.setDisplayValue(afterLow, afterHigh);
				return true;
			}
			return false;
		}
		return true;
	}
}
