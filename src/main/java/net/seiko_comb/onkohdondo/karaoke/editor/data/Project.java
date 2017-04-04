package net.seiko_comb.onkohdondo.karaoke.editor.data;

import java.util.ArrayList;
import java.util.List;

import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputControler;

public class Project implements OutputControler {
	private List<Track> tracks = new ArrayList<>();

	public Project() {

	}

	public List<Track> getTracks() {
		return tracks;
	}

	public double getFullTimeDuration() {
		return tracks.stream().mapToDouble(Track::getFullTimeDuration).reduce(0,
				Double::max);
	}

	@Override
	public double getOutput(int channel, double time) {
		double ret = 0;
		for (Track track : tracks) {
			ret += track.getOutput(channel, time);
		}
		return ret;
	}
}
