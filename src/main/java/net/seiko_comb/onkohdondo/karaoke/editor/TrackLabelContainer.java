package net.seiko_comb.onkohdondo.karaoke.editor;

import net.seiko_comb.onkohdondo.uihelper.Container;
import processing.event.MouseEvent;

public class TrackLabelContainer extends Container<MainPApplet> {

	public TrackLabelContainer(MainPApplet p) {
		super(p);
	}

	protected TrackContainer trackContainer;

	protected void setTrackContainer(TrackContainer trackContainer) {
		this.trackContainer = trackContainer;
	}

	public final float headerHeight = 15;

	@Override
	protected void draw() {
		p.strokeWeight(1);
		p.stroke(0);
		p.fill(255, 255, 232);

		p.rect(0, 0, width, height);

		p.noStroke();
		p.fill(212, 0, 0);
		p.rect(width, 0, -headerHeight, headerHeight);
		p.stroke(255);
		cross(width - headerHeight * 0.8f, headerHeight * 0.2f,
				width - headerHeight * 0.2f, headerHeight * 0.8f);

		p.stroke(128);
		p.line(0, headerHeight, width, headerHeight);
	}

	private void cross(float x1, float y1, float x2, float y2) {
		p.line(x1, y1, x2, y2);
		p.line(x1, y2, x2, y1);
	}

	@Override
	public boolean mouseClicked(MouseEvent event, float mouseX, float mouseY) {
		if (super.mouseClicked(event, mouseX, mouseY)) return true;
		if (width - headerHeight <= mouseX && mouseY < headerHeight) {
			trackContainer.removeTrack();
			return true;
		}
		return false;
	}
}
