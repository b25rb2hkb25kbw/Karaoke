package net.seiko_comb.onkohdondo.karaoke.editor.data;

import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputControler;

public class Track implements OutputControler {

	public Track() {
	}

	public double getFullTimeDuration() {
		return 0;
	}

	@Override
	public double getOutput(int channel, double time) {
		return 0;
	}

}
