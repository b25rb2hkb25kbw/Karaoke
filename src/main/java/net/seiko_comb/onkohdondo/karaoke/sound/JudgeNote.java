package net.seiko_comb.onkohdondo.karaoke.sound;

public class JudgeNote {
	private KaraokeNote note;
	private double start, end;
	private int pitch;

	private boolean judged = false;
	private double judgePitch = -1;

	public JudgeNote(KaraokeNote note, double start, double end) {
		this.note = note;
		this.start = start;
		this.end = end;
		this.pitch = note.getPitch();
	}

	public void judge(MidiReader reader, SoundInputThread in, boolean demo) {
		long startMillis = reader.getMillisFromSeconds(start);
		long endMillis = reader.getMillisFromSeconds(end);
		if (demo) {
			judgePitch = pitch;
		} else {
			if (!in.hasData(endMillis))
				return;
			judgePitch = in.nearest(pitch, startMillis, endMillis).orElse(-1.0);
		}
		judged = true;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	public double getJudgePitch() {
		return judgePitch;
	}

	public boolean isJudged() {
		return judged;
	}

	public int getDisplayJudgePitch() {
		return (int) Math.round(judgePitch);
	}

	public KaraokeNote getNote() {
		return note;
	}

	public double getJudgeScore() {
		if (judgePitch == -1)
			return 0;
		double delta = judgePitch - pitch;
		delta += 6;
		delta = (delta % 12 + 12) % 12;
		delta -= 6;
		if (delta <= 1.5 / 8) {
			return 1;
		} else if (delta <= 2.5 / 8) {
			return 0.0;
		} else if (delta <= 3.5 / 8) {
			return 0.0;
		} else if (delta <= 4.5 / 8) {
			return 0.0;
		} else if (delta <= 5.5 / 8) {
			return 0.0;
		} else if (delta <= 8.5 / 8) {
			return 0.0;
		} else {
			return 0;
		}
	}
}
