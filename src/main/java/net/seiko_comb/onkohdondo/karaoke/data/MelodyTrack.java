package net.seiko_comb.onkohdondo.karaoke.data;

import java.util.List;

import net.seiko_comb.onkohdondo.karaoke.editor.data.Track;

public class MelodyTrack extends Track {

	private TempoTrack tempo;
	private List<Note> notes;

	public MelodyTrack(TempoTrack tempo) {
		this.tempo = tempo;
	}

	public TempoTrack getTempo() {
		return tempo;
	}

	public void setTempo(TempoTrack tempo) {
		this.tempo = tempo;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

	public static class Note {
		private int pitch;
		private double start, end;

		public Note(int pitch, double start, double end) {
			this.pitch = pitch;
			this.start = start;
			this.end = end;
		}

		public int getPitch() {
			return pitch;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}
	}
}
