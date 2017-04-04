package net.seiko_comb.onkohdondo.karaoke.editor;

import java.util.function.Consumer;

import net.seiko_comb.onkohdondo.karaoke.data.MelodyTrack;
import net.seiko_comb.onkohdondo.karaoke.data.TempoTrack;
import net.seiko_comb.onkohdondo.karaoke.editor.data.PowerGraphTrack;
import net.seiko_comb.onkohdondo.karaoke.editor.data.SoundTrack;
import net.seiko_comb.onkohdondo.karaoke.editor.data.Track;
import net.seiko_comb.onkohdondo.uihelper.Container;

public class TrackContainer extends Container<MainPApplet> {
	private Track track;
	private TimelineContainer timelineContainer;
	private TrackLabelContainer trackLabelContainer;
	private TrackContentsContainer trackContentsContainer;

	private float preferredHeight = 400;

	private TrackContainer(MainPApplet p, TimelineContainer timelineContainer,
			TrackLabelContainer trackLabelContainer,
			TrackContentsContainer trackContentsContainer, Track track) {
		super(p);
		this.track = track;
		this.timelineContainer = timelineContainer;
		this.trackLabelContainer = trackLabelContainer;
		this.trackContentsContainer = trackContentsContainer;
	}

	public static TrackContainer getTrackContainer(MainPApplet p,
			TimelineContainer timelineContainer, Track track) {
		TrackContainer container;
		if (track instanceof SoundTrack) {
			container = new TrackContainer(p, timelineContainer,
					new SoundTrackLabelContainer(p, (SoundTrack) track),
					new SoundTrackContentsContainer(p, (SoundTrack) track),
					track);
			container.preferredHeight = 300;
		} else if (track instanceof PowerGraphTrack) {
			container = new TrackContainer(p, timelineContainer,
					new TrackLabelContainer(p),
					new PowerGraphTrackContentsContainer(p,
							(PowerGraphTrack) track),
					track);
			container.preferredHeight = 150;
		} else if (track instanceof TempoTrack) {
			container = new TrackContainer(p, timelineContainer,
					new TrackLabelContainer(p),
					new TempoTrackContentsContainer(p, (TempoTrack) track),
					track);
			container.preferredHeight = 50;
		} else if (track instanceof MelodyTrack) {
			container = new TrackContainer(p, timelineContainer,
					new TrackLabelContainer(p),
					new MelodyTrackContentsContainer(p, (MelodyTrack) track), track);
		} else {
			container = new TrackContainer(p, timelineContainer,
					new TrackLabelContainer(p), new TrackContentsContainer(p),
					track);
		}
		container.trackContentsContainer.setTrackContainer(container);
		container.trackLabelContainer.setTrackContainer(container);
		return container;
	}

	@Override
	protected void draw() {
		height = preferredHeight;
		drawTimeline();
	}

	private void drawTimeline() {
		initDrawCoordinates();

		redrawToLeft(timelineContainer.getTimelineContainerLabelWidth(),
				trackLabelContainer);
		redrawToCenter(trackContentsContainer);
	}

	public Track getTrack() {
		return track;
	}

	public TimelineContainer getTimelineContainer() {
		return timelineContainer;
	}

	@Override
	public void forEachChildContainer(Consumer<Container<?>> c) {
		c.accept(trackLabelContainer);
		c.accept(trackContentsContainer);
	}

	public void removeTrack() {
		p.getEditorManager().removeTrack(track);
	}
}
