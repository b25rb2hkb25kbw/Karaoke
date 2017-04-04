package net.seiko_comb.onkohdondo.karaoke.data;

public class SoundChannel {
	private SoundFile soundFile;
	private int channelIndex;
	private double[] waveform;

	public SoundChannel(SoundFile soundFile, int channelIndex,
			double[] waveform) {
		this.soundFile = soundFile;
		this.channelIndex = channelIndex;
		this.waveform = waveform;
	}

	public SoundFile getSoundFile() {
		return soundFile;
	}

	public int getChannelIndex() {
		return channelIndex;
	}

	public double[] getWaveform() {
		return waveform;
	}

}
