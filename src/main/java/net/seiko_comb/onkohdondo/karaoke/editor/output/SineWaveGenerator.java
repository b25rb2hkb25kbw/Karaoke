package net.seiko_comb.onkohdondo.karaoke.editor.output;

public class SineWaveGenerator implements OutputControler {

	double frequency;

	public SineWaveGenerator(double frequency) {
		this.frequency = frequency;
	}

	@Override
	public double getOutput(int channel, double time) {
		return Math.sin(time / (1 / frequency) * 2 * Math.PI);
	}

}
