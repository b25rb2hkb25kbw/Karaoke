package net.seiko_comb.onkohdondo.karaoke.sound;

public class KaraokePage {
	private double start, end;
	private boolean endDecided = false;

	public KaraokePage(double start) {
		this.start = start;
	}

	public boolean isEndDecided() {
		return endDecided;
	}

	public void setEnd(double end) {
		if (!endDecided) {
			this.end = end;
			endDecided = true;
		}
	}

	public String toString() {
		return String.format(
				"KaraokePage [%.3f -> " + (endDecided ? "%.3f " : "") + "]",
				start, end);
	}

	public boolean contains(KaraokeNote note) {
		return start - 1E-3 <= note.getStartTime()
				&& note.getEndTime() <= end + 1E-3;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

}
