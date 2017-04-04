package net.seiko_comb.onkohdondo.karaoke.editor;

import net.seiko_comb.onkohdondo.karaoke.editor.data.PowerGraphTrack;
import processing.core.PApplet;

public class PowerGraphTrackContentsContainer extends TrackContentsContainer {
	private PowerGraphTrack powerGraphTrack;

	public PowerGraphTrackContentsContainer(MainPApplet p,
			PowerGraphTrack track) {
		super(p);
		this.powerGraphTrack = track;
	}

	@Override
	protected void draw() {
		backgroundArea(p.color(0));

		// if (powerGraphTrack.isCalculating()) {
		// p.stroke(255);
		// p.line(0, 0, width, height);
		// } else {
		p.strokeWeight(1);
		p.stroke(255);
		float overallMax = (float) Math
				.max(powerGraphTrack.getResultList().overallMax(), 1e-8);
		// overallMax = 600;
		for (int i = 0; i < width; i++) {
			double tLow = trackContainer.getTimelineContainer()
					.getTimeOnPixelX(i);
			double tHigh = trackContainer.getTimelineContainer()
					.getTimeOnPixelX(i + 1);
			float min = powerGraphTrack.getListMin(tLow, tHigh);
			float max = powerGraphTrack.getListMax(tLow, tHigh);
			float minY = PApplet.map(min, overallMax, 0, 10, height - 10);
			float maxY = PApplet.map(max, overallMax, 0, 10, height - 10);
			p.line(i, minY, i, maxY);
		}
		// }
	}
}
