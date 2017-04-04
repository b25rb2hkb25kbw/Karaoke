package net.seiko_comb.onkohdondo.karaoke.editor;

import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import net.seiko_comb.onkohdondo.karaoke.data.MelodyTrack;
import net.seiko_comb.onkohdondo.karaoke.data.SoundFile;
import net.seiko_comb.onkohdondo.karaoke.data.TempoTrack;
import net.seiko_comb.onkohdondo.karaoke.editor.data.PowerGraphTrack;
import net.seiko_comb.onkohdondo.karaoke.editor.data.Project;
import net.seiko_comb.onkohdondo.karaoke.editor.data.SoundTrack;
import net.seiko_comb.onkohdondo.karaoke.editor.data.Track;
import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputControler;
import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputControlerStatus;
import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputManager;

public class EditorManager {

	private MainPApplet p;
	private Project project;

	private OutputManager out;
	private Optional<OutputControlerStatus> outputStatus = Optional.empty();

	public EditorManager(MainPApplet p) {
		this.p = p;
		project = new Project();

		out = new OutputManager();
	}

	public MainPApplet getMainPApplet() {
		return p;
	}

	public void requestFileManager(String message, Predicate<Path> predicate) {
		p.getBaseContainer().requestFileChooser(message, predicate);
	}

	public void closeFileChooser() {
		p.getBaseContainer().closeFileChooser();
	}

	public void addAudioTrackFromFile() {
		requestFileManager("オーディオファイルを追加", path -> {
			return SoundFile.getFromFile(path).map(soundFile -> {
				project.getTracks().add(new SoundTrack(soundFile));
				return true;
			}).orElse(false);
		});
	}

	public Project getProject() {
		return project;
	}

	private DoubleUnaryOperator getTime;

	public void playSound() {
		playSound(0);
	}

	public void playSound(double start) {
		startPlaying(t -> start + t);
	}

	public void playSound(double start, double end) {
		startPlaying(t -> start + t % (end - start));
	}

	private void startPlaying(DoubleUnaryOperator getTime) {
		if (playing()) return;
		this.getTime = getTime;
		OutputControler controler = new ProjectPlayer();
		OutputControlerStatus c = out.addOutputControler(controler);
		outputStatus = Optional.of(c);
	}

	public void stopSound() {
		outputStatus = Optional.empty();
	}

	public boolean playing() {
		return outputStatus.isPresent();
	}

	public Optional<Double> getPlayingSecond() {
		return outputStatus.map(OutputControlerStatus::getTime)
				.map(getTime::applyAsDouble);
	}

	private class ProjectPlayer implements OutputControler {
		@Override
		public double getOutput(int channel, double time) {
			return project.getOutput(channel, getTime.applyAsDouble(time));
		}

		@Override
		public boolean end() {
			return !playing();
		}
	}

	public void addPowerViewerTrack(SoundTrack soundTrack) {
		project.getTracks().add(new PowerGraphTrack(soundTrack));
	}

	public TempoTrack addTempoTrack() {
		TempoTrack track = new TempoTrack();
		project.getTracks().add(track);
		return track;
	}

	public void removeTrack(Track trackToRemove) {
		List<Track> tracks = project.getTracks();
		for (ListIterator<Track> it = tracks.listIterator(); it.hasNext();) {
			Track track = it.next();
			if (track == trackToRemove) {
				it.remove();
			}
		}
	}

	public <T extends Track> void editTrack(T track, Consumer<T> operation) {
		editTrack(track, operation, Optional.empty());
	}

	public <T extends Track> void editTrack(T track, Consumer<T> operation,
			Optional<Consumer<T>> counterOperation) {
		operation.accept(track);
	}

	public <T extends Track, U> U editTrackAndGet(T track,
			Function<T, U> operation) {
		return editTrackAndGet(track, operation, Optional.empty());
	}

	public <T extends Track, U> U editTrackAndGet(T track,
			Function<T, U> operation,
			Optional<Function<T, U>> counterOperation) {
		return operation.apply(track);
	}

	public void addMelodyTrack() {
		TempoTrack tempoTrack = null;
		for (Track t : project.getTracks())
			if (t instanceof TempoTrack) tempoTrack = (TempoTrack) t;
		if (tempoTrack == null) tempoTrack = addTempoTrack();
		addMelodyTrack(tempoTrack);
	}

	public MelodyTrack addMelodyTrack(TempoTrack tempoTrack) {
		MelodyTrack track = new MelodyTrack(tempoTrack);
		project.getTracks().add(track);
		return track;
	}

}
