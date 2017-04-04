package net.seiko_comb.onkohdondo.karaoke.data;

import java.util.function.DoubleUnaryOperator;

public class TempoStake {

	private final TempoTrack tempo;
	private double beat, second;

	public TempoStake(TempoTrack tempo, double beat, double second) {
		this.tempo = tempo;
		this.beat = beat;
		this.second = second;
	}

	public TempoTrack getParentTempo() {
		return tempo;
	}

	public double getBeat() {
		return beat;
	}

	public double getSecond() {
		return second;
	}

	protected void setSecond(DoubleUnaryOperator change) {
		second = change.applyAsDouble(second);
	}

	protected void setBeat(DoubleUnaryOperator change) {
		beat = change.applyAsDouble(beat);
	}

	@Override
	public String toString() {
		return String.format("[%.3f:%.3f]", second, beat);
	}
}
