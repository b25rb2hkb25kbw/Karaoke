package net.seiko_comb.onkohdondo.karaoke.editor.data;

import java.util.ArrayList;
import java.util.List;

import net.seiko_comb.onkohdondo.karaoke.data.SoundChannel;
import net.seiko_comb.onkohdondo.karaoke.data.SoundFile;
import net.seiko_comb.onkohdondo.util.MinMaxListDouble;

public class SoundTrack extends Track {

	private SoundFile soundFile;
	private List<Channel> channels = new ArrayList<>();

	public SoundTrack(SoundFile soundFile) {
		this.soundFile = soundFile;
		for (int i = 0; i < soundFile.getChannelCount(); i++) {
			Channel channel = new Channel(soundFile.getChannels().get(i));
			channels.add(channel);
		}
	}

	public SoundFile getSoundFile() {
		return soundFile;
	}

	public Channel getChannel(int channelIndex) {
		return channels.get(channelIndex);
	}

	@Override
	public double getFullTimeDuration() {
		return soundFile.getFullTimeDuration();
	}

	public class Channel {
		private SoundChannel channel;
		private MinMaxListDouble list;

		private Channel(SoundChannel channel) {
			this.channel = channel;
			list = new MinMaxListDouble(channel.getWaveform());
		}

		public SoundChannel getChannel() {
			return channel;
		}

		public double min(double from, double to) {
			return list.min(from * soundFile.getSampleRate(),
					to * soundFile.getSampleRate());
		}

		public double max(double from, double to) {
			return list.max(from * soundFile.getSampleRate(),
					to * soundFile.getSampleRate());
		}

		private double getDataAtTime(double seconds) {
			return list.getDataAt(seconds * soundFile.getSampleRate());
		}
	}

	@Override
	public double getOutput(int channel, double time) {
		return channels.get(channel).getDataAtTime(time);
	}
}
