package net.seiko_comb.onkohdondo.karaoke.sound;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.sampled.LineUnavailableException;

import processing.core.PApplet;
import processing.core.PGraphics;

public class MidiReader extends PApplet {

	private boolean demo = false;
	private String root = "C:/Users/ksam3/tmp/Kareoke/";
	private String songName = "kiniroginiromokusei";

	public static void main(String[] args) {
		PApplet.main(MidiReader.class.getName());
	}

	@Override
	public void settings() {
		size(1280, 720);
	}

	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F",
			"F#", "G", "G#", "A", "A#", "B" };

	public List<KaraokeNote> notesList;
	public List<JudgeNote> judgeNotes;
	public List<KaraokePage> karaokePages;

	private SoundInputThread in;

	@Override
	@SuppressWarnings("unused")
	public void setup() {
		Sequence sequence = null;
		try {
			sequence = MidiSystem
					.getSequence(new File(root + songName + ".mid"));
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}

		Track conductorTrack = sequence.getTracks()[0];
		Track melodyTrack = sequence.getTracks()[2];
		Track pageTrack = sequence.getTracks()[3];
		notesList = new ArrayList<>();
		judgeNotes = new ArrayList<>();
		karaokePages = new ArrayList<>();
		Map<Integer, KaraokeNote> noteMap = new HashMap<>();

		double bpmStartTime = 0.0, timePerBeat = 0.5;
		long bpmStartTick = 0;
		final double resolution = sequence.getResolution();

		for (MidiEvent event : (Iterable<MidiEvent>) (Stream
				.of(conductorTrack, melodyTrack, pageTrack)
				.flatMap(MidiReader::getEvent).sorted(Comparator
						.comparingLong(MidiEvent::getTick))::iterator)) {
			long tick = event.getTick();
			double time = bpmStartTime
					+ (tick - bpmStartTick) / resolution * timePerBeat;
			// System.out.format("@%6d %4.3f ", tick, time);

			MidiMessage message = event.getMessage();
			if (message instanceof MetaMessage) {
				MetaMessage metaMessage = (MetaMessage) message;
				int type = metaMessage.getType();
				byte[] data = metaMessage.getData();
				// Consumer<String> showText = label -> System.out
				// .format("%s : %s%n", label, getStringFromByte(data));
				if (type == 0x01) {
					// showText.accept("Text event");
				} else if (type == 0x02) {
					// showText.accept("Copyright");
				} else if (type == 0x03) {
					// showText.accept("Sequence/Track name");
				} else if (type == 0x06) {
					// showText.accept("Marker");
				} else if (type == 0x51) {
					int beatMicros = (data[0] & 0xff) << 16
							| (data[1] & 0xff) << 8 | (data[2] & 0xff);
					// System.out.format("Tempo : %d %.3f%n", beatMicros,
					// 60.0 * 1000 * 1000 / beatMicros);
					timePerBeat = beatMicros / 1000.0 / 1000.0;
					bpmStartTick = tick;
					bpmStartTime = time;
				} else if (type == 0x58) {
					// System.out.format(
					// "Time Signiture : %d/%d, %d clicks, quater notes=%d%n",
					// data[0], 1 << data[1], data[2], data[3]);
				} else {
					// System.out.format("%02x", type);
					// for (byte b : data)
					// System.out.format(" %02x", b);
					// System.out.format("%n");
				}
			}

			double nowTimePerBeat = timePerBeat;
			ShortMessage s = null;
			ifShortMessage(message, shortMessage -> {
				int channel = shortMessage.getChannel();
				if (channel == 0) {
					if (shortMessage.getCommand() == NOTE_ON) {
						int key = shortMessage.getData1();
						int octave = (key / 12) - 1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						int velocity = shortMessage.getData2();
						// System.out.format(
						// "Ch%02d Note on, %s%d(%d), velocity=%d%n",
						// shortMessage.getChannel(), noteName, octave, key,
						// velocity);
						// System.out.println("Note on, " + noteName + octave +
						// "key="
						// + key + " velocity: " + velocity);
						KaraokeNote karaokeNote = new KaraokeNote(key, time);
						notesList.add(karaokeNote);
						noteMap.put(key, karaokeNote);
					} else if (shortMessage.getCommand() == NOTE_OFF) {
						int key = shortMessage.getData1();
						int octave = (key / 12) - 1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						int velocity = shortMessage.getData2();
						// System.out.println("Note off, " + noteName + octave
						// + " key=" + key + " velocity: " + velocity);
						Optional.ofNullable(noteMap.get(key)).ifPresent(n -> {
							n.setEndTime(time, nowTimePerBeat);
							n.getJudgeNotes().forEach(judgeNotes::add);
						});
					} else {
						// System.out.println("Command:" +
						// shortMessage.getCommand());
						// System.out.println();
					}
				} else if (channel == 1) {
					int size = karaokePages.size();
					Optional<KaraokePage> lastPage = Optional.empty();
					if (size > 0)
						lastPage = Optional.of(karaokePages.get(size - 1));
					lastPage = lastPage.map(p -> p.isEndDecided() ? null : p);
					if (shortMessage.getCommand() == NOTE_ON) {
						if (!lastPage.isPresent())
							karaokePages.add(new KaraokePage(time));
					} else if (shortMessage.getCommand() == NOTE_OFF) {
						lastPage.ifPresent(p -> p.setEnd(time));
					}
				}
			});
		}

		outputManager = new OutputManager();

		try {
			in = new SoundInputThread(9, 2048);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			exit();
		}

		if (judgeNotes.size() > 0) {
			double pitch = judgeNotes.get(0).getNote().getPitch();
			for (int i = 0; i < 10; i++) {
				starPitchBuffer[i] = pitch;
			}
		}

		for (int i = 0; i < 100; i++) {
			overStarY[i] = random(-1, 1);
			overStarR[i] = random(-1, 1);
		}
	}

	@Override
	public void keyTyped() {
		String file = "";
		switch (songName) {
		case "konosuba":
			file = "C:/Users/ksam3/Music/アニメ/この素晴らしい世界に祝福を！/[160127] TVアニメ「この素晴らしい世界に祝福を！」OPテーマ「fantastic dreamer」／Machico [320K]/04. fantastic dreamer (Off Vocal Ver.).ogg";
//			file = "C:/Users/ksam3/Music/アニメ/この素晴らしい世界に祝福を！/"
//					+ "[160127] TVアニメ「この素晴らしい世界に祝福を！」"
//					+ "OPテーマ「fantastic dreamer」／Machico[320K]/"
//					+ "04. fantastic dreamer (Off Vocal Ver.).ogg";
			// readWave(folder + "04. fantastic dreamer (Off Vocal Ver.).wav");
			break;
		case "thisgame":
			file = "C:/Users/ksam3/Music/アニメ/ノーゲーム・ノーライフ/"
					+ "This game (OP)/04 - This game (Instrumental).ogg";
			break;
		case "sakuraskip":
			file = "C:/Users/ksam3/Music/アニメ/NEW GAME!/"
					+ "[160727] TVアニメ「NEW GAME!」OPテーマ「SAKURAスキップ」／fourfolium [MP3]/"
					+ "03. SAKURAスキップ (Instrumental).ogg";
			break;
		case "nowloading":
			file = "C:/Users/ksam3/Music/アニメ/NEW GAME!/"
					+ "[160727] TVアニメ「NEW GAME!」EDテーマ「Now Loading!!!!」／fourfolium [MP3]/"
					+ "03. Now Loading!!!! (Instrumental).ogg";
			break;
		case "kiniroginiromokusei":
			file = "C:/Users/ksam3/Music/アニメ/きんいろモザイク/"
					+ "[130821] きんいろモザイク サウンドブック「はじめまして よろしくね。 」/FLAC/"
					+ "26. きんいろ+ぎんいろモクセイ.ogg";
			break;
		case "ewosagasunichijou":
			file = "C:/Users/ksam3/Music/Audacity/04. Eを探す日常 (Instrumental).ogg";
			break;
		}
		if (startMillis == -1) {
			startMillis = System.currentTimeMillis() + 5000;
			outputManager.playSong(new File(file), startMillis, 1.0);
			// new OutputManager().playSong(new File(root + songName + ".ogg"),
			// startMillis, 2.0);
		}
	}
	// @Deprecated
	// private void readWave(String fileName, double speed) {
	// byte[] audioBytes = null;
	//
	// try {
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// BufferedInputStream in = new BufferedInputStream(
	// new FileInputStream(fileName));
	// int read;
	// byte[] buff = new byte[1024];
	// while ((read = in.read(buff)) > 0) {
	// out.write(buff, 0, read);
	// }
	// out.flush();
	// byte[] bytes = out.toByteArray();
	// audioBytes = Arrays.copyOf(bytes, bytes.length / 4 * 4);
	// } catch (IOException e) {
	// System.err.println("Could not read sound file.");
	// e.printStackTrace();
	// }
	//
	// PGraphics image = waveImage = createGraphics(
	// (int) (audioBytes.length / 4 / 44100.0 * speed),
	// (int) (height * 0.2f));
	//
	// image.beginDraw();
	// image.background(0);
	// image.stroke(255);
	// float beforeX = 0, beforeY = height / 2;
	// int percentageBefore = 0;
	// for (int i = 0; i < audioBytes.length / 4 / 20; i++) {
	// int percentage = i * 100 / (audioBytes.length / 4);
	// if (percentageBefore != percentage) {
	// percentageBefore = percentage;
	// }
	// double seconds = i / 44100.0;
	// float x = (float) (seconds * speed);
	// int val = ((audioBytes[i * 4 + 0] & 0xff)
	// | (audioBytes[i * 4 + 1] << 8))
	// + ((audioBytes[i * 4 + 2] & 0xff)
	// | (audioBytes[i * 4 + 3] << 8));
	// float y = map(val, 65536, -65536, 0, image.height);
	// image.line(beforeX, beforeY, x, y);
	// beforeX = x;
	// beforeY = y;
	// }
	// image.endDraw();
	// }

	// private PGraphics waveImage;

	private OutputManager outputManager;
	private long startMillis = -1;

	private int judgeIndex = 0;
	private int judgeStarIndex = 0;
	private int pageIndex = 0;

	private double[] starPitchBuffer = new double[300];
	private int starDuration = 100;
	private int starNoteIndex = 0;
	private double lastPitch = 0;

	private double judgeScore = 0;
	private double judgeFullScore = 0;
	private double pageJudgeScore = 0;
	private double pageFullScore = 0;
	private double lastPageScore = 0;

	private double overStarStart = -1000;
	private double overStarEnd = -999;
	private Optional<KaraokePage> overPage = Optional.empty();
	private float[] overStarY = new float[100];
	private float[] overStarR = new float[100];

	@Override
	public void draw() {
		double seconds = getSeconds();
		int bottomPitch = 61, topPitch = 85;
		float chartTop = height / 10, chartBottom = height / 3;
		float left = width / 20, right = width - left;
		DoubleFunction<Float> pitchToY = p -> (float) map(p, topPitch,
				bottomPitch, chartTop, chartBottom);
		float noteHeight = (chartBottom - chartTop) / (topPitch - bottomPitch)
				* 2;

		background(0);

		// pitch scale
		for (int i = bottomPitch; i <= topPitch; i += 2) {
			float y = pitchToY.apply(i);
			stroke(128);
			line(0, y, width, y);
		}

		fill(255);
		textAlign(RIGHT, TOP);
		text(String.format("%.3f%%", judgeScore / judgeFullScore * 100), width,
				0);
		text(String.format("%.3f%%", lastPageScore * 100), width, 40);

		if (startMillis == -1) return;
		getPage().ifPresent(page -> {
			if (seconds < page.getEnd() + 1.0) {
				double pixelPerSecond = (right - left)
						/ (page.getEnd() - page.getStart());
				DoubleFunction<Float> timeToX = t -> left
						+ (float) ((t - page.getStart()) * pixelPerSecond);

				notesList.stream().filter(page::contains).forEach(
						note -> drawNote(pitchToY, noteHeight, timeToX, note));

				// cursor bar
				strokeWeight(3);
				stroke(255, 242, 0);
				float timeBarX = timeToX.apply(getSeconds());
				line(timeBarX, chartTop, timeBarX, chartBottom);

				// pitch star
				int count = 3;
				long millis = System.currentTimeMillis() - 400;
				millis = millis / starDuration * starDuration;
				while (!in.hasData(millis))
					millis -= starDuration;
				fill(128, 128, 255);
				strokeWeight(1);
				for (int i = 0; i < count; i++) {
					long m = millis;
					int j = i;
					in.median(starPitchBuffer[getStarBufferIndex(m)],
							millis - starDuration, millis).ifPresent(median -> {
								stroke(0, 0, 255);
								fill(255);
								ellipse(timeToX.apply(getSecondsFromMillis(m)),
										pitchToY.apply(median), 6 - j * 2,
										6 - j * 2);
							});
					millis -= starDuration;
				}
			}
			double progress = map(seconds, page.getStart(), page.getEnd(), 0,
					1);
			if (progress > 0.8) {
				if (pageIndex == judgeStarIndex) {
					lastPageScore = getPageJudgeScore();
					judgeStarIndex++;
					overStarStart = overStarEnd = seconds;
					overStarEnd += Math.max(
							(page.getEnd() - page.getStart()) / 5 - 0.3, 0.7);
					overPage = Optional.of(page);
				}
			}
			if (progress > 0.97) {
				getNextPage().ifPresent(nextPage -> {
					if (nextPage.getStart() - 3.0 < seconds) pageIndex++;
				});
			}
		});

		// overstar
		overPage.ifPresent(page -> {
			for (int i = 0; i < 100; i++) {
				float x = map(i, 0, 100, left, right);
				double t = seconds - map(i, 0, 100, overStarStart, overStarEnd);
				long m = getMillisFromSeconds(
						map(i, 0, 100, page.getStart(), page.getEnd()));
				double p = starPitchBuffer[getStarBufferIndex(m)];
				float y = pitchToY.apply(p) + overStarY[i] * noteHeight * 2;
				float r = overStarR[i] * 10;
				if (lastPageScore >= 0.9) {
					colorMode(HSB);
					noStroke();
					fill((float) map(overStarY[i], -1.0, 1.0, 170 + 255,
							-40 + 255) % 255, 204, 230);
					colorMode(RGB);
				} else if (lastPageScore >= 0.8) {
					stroke(255, 204, 0);
				} else if (lastPageScore >= 0.7) {
					stroke(255, 32, 32);
				} else if (lastPageScore >= 0.6) {
					stroke(0, 0, 255);
				} else {
					noStroke();
					noFill();
				}
				if (t < 0) {
				} else if (t < 0.3) {
					r = map((float) t, 0, 0.3f, 0, r);
					ellipse(x, y, r, r);
				} else if (t < 0.6) {
					r = map((float) t, 0.3f, 0.6f, r, 0);
					ellipse(x, y, r, r);
				}
			}
		});

		// judge
		if (judgeIndex < judgeNotes.size()) {
			JudgeNote judgeNote = judgeNotes.get(judgeIndex);
			judgeNote.judge(this, in, demo);
			if (judgeNote.isJudged()) {
				// System.out.println(judgeNote.getJudgeScore());
				judgeScore += judgeNote.getJudgeScore();
				judgeFullScore += 1;
				judgeIndex++;
			}
		}

		// star buffer
		long millis = System.currentTimeMillis();
		if (starNoteIndex < judgeNotes.size()) {
			JudgeNote judgeNote = judgeNotes.get(starNoteIndex);
			if (judgeNote.getStart() < seconds) {
				lastPitch = judgeNote.getNote().getPitch();
				starNoteIndex++;
			}
		}
		starPitchBuffer[getStarBufferIndex(millis)] = lastPitch;

		// image(waveImage, width / 4 + (float) (-time * speed), height * 0.4f);
	}

	private double getPageJudgeScore() {
		double ret = (judgeScore - pageJudgeScore)
				/ (judgeFullScore - pageFullScore);
		if (Double.isNaN(ret)) ret = 0;
		pageJudgeScore = judgeScore;
		pageFullScore = judgeFullScore;
		return ret;
	}

	private int getStarBufferIndex(long millis) {
		return (int) ((millis / 100) % starPitchBuffer.length);
	}

	private Optional<KaraokePage> getKaraokePage(int index) {
		Optional<KaraokePage> pageOpt = Optional.empty();
		if (0 <= index && index < karaokePages.size())
			pageOpt = Optional.of(karaokePages.get(index));
		return pageOpt;
	}

	private Optional<KaraokePage> getPage() {
		return getKaraokePage(pageIndex);
	}

	private Optional<KaraokePage> getNextPage() {
		return getKaraokePage(pageIndex + 1);
	}

	private void drawNote(DoubleFunction<Float> pitchToY, float noteHeight,
			DoubleFunction<Float> timeToX, KaraokeNote note) {
		float startX = timeToX.apply(note.getStartTime());
		float endX = timeToX.apply(note.getEndTime());
		float y = (float) pitchToY.apply(note.getPitch());
		stroke(255 * 2 / 3, 204 * 2 / 3, 0);
		fill(255 / 10, 204 / 10, 0);
		rect(startX, y, endX - startX, noteHeight, noteHeight / 2);
		int pitch = -1;
		double start = -1, end = -1;
		BiFunction<Double, Double, Consumer<Integer>> drawNote = (s,
				e) -> p -> {
					double e2 = Math.min(e, getSeconds() - 0.5);
					if (e2 < s) return;
					float sx = timeToX.apply(s);
					float ex = timeToX.apply(e2);
					float y2 = (float) pitchToY.apply(p);
					if (p == note.getPitch())
						fill(255, 204, 0);
					else fill(128, 102, 0);
					rect(sx, y2, ex - sx, noteHeight, noteHeight / 2);
				};
		for (JudgeNote judgeNote : note.getJudgeNotes()) {
			if (judgeNote.getDisplayJudgePitch() != -1) {
				if (pitch == -1) {
					start = judgeNote.getStart();
					end = judgeNote.getEnd();
					pitch = judgeNote.getDisplayJudgePitch();
					continue;
				}
				if (end == judgeNote.getStart()
						&& pitch == judgeNote.getDisplayJudgePitch()) {
					end = judgeNote.getEnd();
				} else {
					drawNote.apply(start, end).accept(pitch);
					start = judgeNote.getStart();
					end = judgeNote.getEnd();
					pitch = judgeNote.getDisplayJudgePitch();
				}
			}
		}
		if (pitch != -1) drawNote.apply(start, end).accept(pitch);
	}

	public double getSeconds() {
		// return millis()/1000.0;
		return getSecondsFromMillis(System.currentTimeMillis());
	}

	public double getSecondsFromMillis(long millis) {
		return (millis - startMillis) / 1000.0;
	}

	public long getMillisFromSeconds(double seconds) {
		return startMillis + (long) (seconds * 1000);
	}

	public String getStringFromByte(byte[] bytes) {
		try {
			return new String(bytes, "Shift-JIS");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static Stream<MidiEvent> getEvent(Track track) {
		return IntStream.range(0, track.size()).mapToObj(track::get);
	}

	@SuppressWarnings("unused")
	private void showMidi() {
		Sequence sequence = null;
		try {
			sequence = MidiSystem.getSequence(new File(songName + ".mid"));
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}

		int trackNumber = 0;
		for (Track track : sequence.getTracks()) {
			trackNumber++;
			System.out.println(
					"Track " + trackNumber + ": size = " + track.size());
			System.out.println();
			for (int i = 0; i < track.size(); i++) {
				MidiEvent event = track.get(i);
				displayEvent(event);
			}
			System.out.println();
		}
	}

	private void displayEvent(MidiEvent event) {
		System.out.format("@%5d ", event.getTick());
		MidiMessage message = event.getMessage();
		if (message instanceof ShortMessage) {
			ShortMessage sm = (ShortMessage) message;
			System.out.print("Channel: " + sm.getChannel() + " ");
			if (sm.getCommand() == NOTE_ON) {
				int key = sm.getData1();
				int octave = (key / 12) - 1;
				int note = key % 12;
				String noteName = NOTE_NAMES[note];
				int velocity = sm.getData2();
				System.out.println("Note on, " + noteName + octave + " key="
						+ key + " velocity: " + velocity);
			} else if (sm.getCommand() == NOTE_OFF) {
				int key = sm.getData1();
				int octave = (key / 12) - 1;
				int note = key % 12;
				String noteName = NOTE_NAMES[note];
				int velocity = sm.getData2();
				System.out.println("Note off, " + noteName + octave + " key="
						+ key + " velocity: " + velocity);
			} else {
				System.out.println("Command:" + sm.getCommand());
			}
		} else {
			ifMetaMessage(message, mes -> {
				System.out.format("%02x", mes.getType());
				for (byte b : mes.getData())
					System.out.format(" %02x", b);
				System.out.format("%n");
			}, () -> {
				System.out.println("Other message: " + message.getClass());
			});
		}
	}

	private void ifShortMessage(MidiMessage message,
			Consumer<ShortMessage> consumer, Runnable... ifElse) {
		if (message instanceof ShortMessage) {
			consumer.accept((ShortMessage) message);
		} else {
			Stream.of(ifElse).forEach(Runnable::run);
		}
	}

	private void ifMetaMessage(MidiMessage message,
			Consumer<MetaMessage> consumer, Runnable... ifElse) {
		if (message instanceof MetaMessage) {
			consumer.accept((MetaMessage) message);
		} else {
			Stream.of(ifElse).forEach(Runnable::run);
		}
	}

	public static <S, T> Optional<T> safeCast(S candidate,
			Class<T> targetClass) {
		return null == targetClass ? Optional.empty()
				: targetClass.isInstance(candidate)
						? Optional.of(targetClass.cast(candidate))
						: Optional.empty();
	}

	static public final double map(double value, double start1, double stop1,
			double start2, double stop2) {
		double outgoing = start2
				+ (stop2 - start2) * ((value - start1) / (stop1 - start1));
		String badness = null;
		if (outgoing != outgoing) {
			badness = "NaN (not a number)";

		} else if (outgoing == Double.NEGATIVE_INFINITY
				|| outgoing == Double.POSITIVE_INFINITY) {
			badness = "infinity";
		}
		if (badness != null) {
			final String msg = String.format(
					"map(%f, %f, %f, %f, %f) called, which returns %s", value,
					start1, stop1, start2, stop2, badness);
			PGraphics.showWarning(msg);
		}
		return outgoing;
	}
}
