package net.seiko_comb.onkohdondo.karaoke.data;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;

public class MeasureStake {
	private final TempoTrack tempo;

	private double beat;
	private int beatCount, baseCount;

	public MeasureStake(TempoTrack tempo, double beat, int beatCount,
			int baseCount) {
		this.tempo = tempo;
		this.beat = beat;
		this.beatCount = beatCount;
		this.baseCount = baseCount;
	}

	public TempoTrack getParentTempo() {
		return tempo;
	}

	public double getBeat() {
		return beat;
	}

	public int getBaseCount() {
		return baseCount;
	}

	public int getBeatCount() {
		return beatCount;
	}

	public double getMeasureBeatLength() {
		return getBaseBeatLength() * beatCount;
	}

	public double getBaseBeatLength() {
		return 480.0 * 4 / baseCount;
	}

	public void setBaseCount(IntUnaryOperator baseCount) {
		this.baseCount = baseCount.applyAsInt(this.baseCount);
	}

	public void setBeatCount(IntUnaryOperator beatCount) {
		this.beatCount = beatCount.applyAsInt(this.beatCount);
	}

	public void setMeasureCounts(IntUnaryOperator beatCount,
			IntUnaryOperator baseCount) {
		setBeatCount(beatCount);
		setBaseCount(baseCount);
	}

	protected void setBeat(DoubleUnaryOperator change) {
		beat = change.applyAsDouble(beat);
	}

	@Override
	public String toString() {
		return String.format("[%.3f:%d/%d]", beat, beatCount, baseCount);
	}
}
