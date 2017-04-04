package net.seiko_comb.onkohdondo.karaoke.editor.output;

import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

public class FrequencyOutputControler implements OutputControler {

	private DoubleUnaryOperator velocity = t -> 1.0;
	private DoubleUnaryOperator waveform;
	private DoubleUnaryOperator frequency = t -> 440;

	private Optional<RecorderAdapter> recorder = Optional.empty();

	public FrequencyOutputControler(DoubleUnaryOperator waveform) {
		this.waveform = waveform;
	}

	private double lastTime = Double.POSITIVE_INFINITY;
	private double lastX;

	@Override
	public double getOutput(int channel, double time) {
		double frequency = this.frequency.applyAsDouble(time);
		if (time < lastTime) {
			lastTime = time;
			lastX = 0;
		} else {
			double timeDiff = time - lastTime;
			lastX += timeDiff * frequency;
			lastTime = time;
		}
		double velocity = this.velocity.applyAsDouble(time);
		recorder.ifPresent(r -> {
			if (r.lastRecorded + r.duration <= time) {
				r.lastRecorded = time;
				r.recorder.record(time, velocity, frequency);
			}
		});
		return velocity * waveform.applyAsDouble(lastX);
	}

	public void setFrequency(double frequency) {
		this.frequency = t -> frequency;
	}

	public void setFrequency(DoubleUnaryOperator frequency) {
		this.frequency = frequency;
	}

	public void setVelocity(double velocity) {
		this.velocity = t -> velocity;
	}

	public void setVelocity(DoubleUnaryOperator velocity) {
		this.velocity = velocity;
	}

	public RecorderAdapter setRecorder(double duration, Recorder recorder) {
		RecorderAdapter ret = new RecorderAdapter(duration, recorder);
		this.recorder = Optional.of(ret);
		return ret;
	}

	public static class RecorderAdapter {
		private double lastRecorded;
		private double duration;
		private Recorder recorder;

		private RecorderAdapter(double duration, Recorder recorder) {
			this.lastRecorded = duration * 2;
			if (duration <= 0.0) {
				throw new IllegalArgumentException();
			}
			this.duration = duration;
			this.recorder = recorder;
		}
	}

	@FunctionalInterface
	public static interface Recorder {
		public void record(double time, double velocity, double frequency);
	}
}
