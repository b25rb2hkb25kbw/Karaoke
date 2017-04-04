package net.seiko_comb.onkohdondo.karaoke.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundFile {
	public static void main(String... args) {
		getFromFile(Paths.get("C:", "Users", "ksam3", "Music", "Audacity",
				"sample.ogg"));
	}

	private static final Pattern EXTENSION_PATTERN = Pattern
			.compile("\\.([^.]+)$");

	private int channelCount;
	private int sampleRate;

	private List<SoundChannel> channels;

	private SoundFile() {

	}

	public static Optional<SoundFile> getFromFile(Path filePath) {
		SoundFile soundFile = new SoundFile();
		Matcher extensionMatcher = EXTENSION_PATTERN
				.matcher(filePath.toString());
		String extension = "";
		if (extensionMatcher.find()) {
			extension = extensionMatcher.group(1);
		}
		switch (extension) {
		case "ogg":
			try {
				soundFile.readOgg(filePath);
				return Optional.of(soundFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		return Optional.empty();
	}

	private void readOgg(Path filePath) throws IOException {
		OggReader oggReader = new OggReader(filePath);
		oggReader.read();
		channelCount = oggReader.getvInfo().channels;
		sampleRate = oggReader.getvInfo().rate;
		channels = new ArrayList<>();
		byte[] result = oggReader.getResult();
		int sampleCount = result.length / 2 / channelCount;
		for (int i = 0; i < channelCount; i++) {
			double[] wave = new double[sampleCount];
			for (int j = 0, k = i * 2; j < sampleCount; j++, k += channelCount
					* 2) {
				int dat = (result[k] & 0xff) + (result[k + 1] << 8);
				wave[j] = dat / 32768.0;
			}
			channels.add(new SoundChannel(this, i, wave));
		}
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public List<SoundChannel> getChannels() {
		return channels;
	}

	public double getFullTimeDuration() {
		return ((double) getSampleCount()) / sampleRate;
	}

	public int getSampleCount() {
		if (channels.size() == 0) return 0;
		return channels.get(0).getWaveform().length;
	}
}
