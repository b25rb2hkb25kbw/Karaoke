package net.seiko_comb.onkohdondo.karaoke.editor;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.seiko_comb.onkohdondo.uihelper.Container;

public class BaseContainer extends Container<MainPApplet> {

	private TimelineContainer timelineContainer;
	private Optional<FileChooserContainer> fileChooserContainer = Optional
			.empty();

	public BaseContainer(MainPApplet p) {
		super(p);
		timelineContainer = new TimelineContainer(p);
	}

	@Override
	public void draw() {
		fileChooserContainer.ifPresent(fileChooser -> {
			redrawToLeft(250, fileChooser);
		});
		redrawToCenter(timelineContainer);
	}

	@Override
	public void forEachChildContainer(
			Consumer<Container<?>> containerConsumer) {
		fileChooserContainer.ifPresent(containerConsumer);
		containerConsumer.accept(timelineContainer);
	}

	protected void requestFileChooser(String message,
			Predicate<Path> predicate) {
		fileChooserContainer = Optional
				.of(new FileChooserContainer(p, message, predicate));
	}

	protected void closeFileChooser() {
		fileChooserContainer = Optional.empty();
	}
}
