package net.seiko_comb.onkohdondo.karaoke.editor;

import net.seiko_comb.onkohdondo.karaoke.data.MelodyTrack;
import net.seiko_comb.onkohdondo.karaoke.data.TempoTrack;

public class MelodyTrackContentsContainer extends TrackContentsContainer {

	private MelodyTrack melodyTrack;

	public MelodyTrackContentsContainer(MainPApplet p, MelodyTrack track) {
		super(p);
		this.melodyTrack = track;
	}

	@Override
	protected void draw() {
		backgroundArea(p.color(0, 216, 204));
		TempoTrack tempoTrack = melodyTrack.getTempo();
		double startBeat = tempoTrack.getBeat(
				trackContainer.getTimelineContainer().getTimeOnPixelX(0));
		double endBeat = tempoTrack.getBeat(
				trackContainer.getTimelineContainer().getTimeOnPixelX(width));
	}
}
