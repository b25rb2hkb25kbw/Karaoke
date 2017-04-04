package net.seiko_comb.onkohdondo.karaoke.sound;

import java.util.ArrayList;
import java.util.List;

public class KaraokeNote {
	private double startTime, endTime;
	private int pitch;
	private List<JudgeNote> judgeNotes = new ArrayList<>();

	public KaraokeNote(int key, double startTime) {
		this.pitch = key;
		this.startTime = startTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public int getPitch() {
		return pitch;
	}

	public void setEndTime(double endTime, double timePerBeat) {
		this.endTime = endTime;
		double nowTime = startTime;
		while(nowTime + 1E-3 < endTime){
			double nextEnd = Math.min(endTime, nowTime + timePerBeat);
			judgeNotes.add(new JudgeNote(this, nowTime, nextEnd));
			nowTime = nextEnd;
		}
	}
	
	@Override
	public String toString() {
		return String.format("[%d %.3f->%.3f]", pitch, startTime, endTime);
	}
	
	public List<JudgeNote> getJudgeNotes() {
		return judgeNotes;
	}
}
