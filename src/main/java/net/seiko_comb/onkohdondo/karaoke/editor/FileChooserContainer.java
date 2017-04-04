package net.seiko_comb.onkohdondo.karaoke.editor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.seiko_comb.onkohdondo.uihelper.Container;
import net.seiko_comb.onkohdondo.uihelper.ScrollBarContainer;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class FileChooserContainer extends Container<MainPApplet>
		implements PConstants {

	private String message;
	private Predicate<Path> pathOperation;

	public FileChooserContainer(MainPApplet p, String message,
			Predicate<Path> predicate) {
		super(p);
		this.message = message;
		this.pathOperation = predicate;

		initGUI();
	}

	private static Optional<Path> currentPath;
	private static List<Path> childPaths = new ArrayList<>();
	static {
		setPath(Paths.get(System.getProperty("user.home")));
	}

	private float headerHeight = 18;
	private float closeButtonWidth = 30;
	private boolean closeButtonMouseOver = false;
	private FileChooserList fileChooserList;
	private float fileChooserListMargin = 4;
	private boolean openingFile = false;

	private void initGUI() {
		fileChooserList = new FileChooserList(p);
	}

	@Override
	protected void draw() {
		backgroundArea(p.color(219, 229, 239));

		p.fill(0);
		p.textAlign(LEFT);
		p.textFont(p.getUiGothic(), headerHeight * .8f);
		p.text(message, 10, headerHeight * .9f);

		if (closeButtonMouseOver) {
			p.noStroke();
			p.fill(232, 17, 35);
			p.rect(width, 0, -closeButtonWidth, headerHeight);
			p.fill(255);
		} else {
			p.fill(128, 0, 0);
		}
		p.textAlign(CENTER);
		p.text("×", width - closeButtonWidth / 2, headerHeight * .9f);

		fileChooserList.redraw(fileChooserListMargin,
				headerHeight + fileChooserListMargin,
				width - fileChooserListMargin * 2,
				height - headerHeight - fileChooserListMargin * 2);
	}

	@Override
	public void forEachChildContainer(
			Consumer<Container<?>> containerConsumer) {
		containerConsumer.accept(fileChooserList);
	}

	@Override
	public boolean mouseClicked(MouseEvent event, float mouseX, float mouseY) {
		if (mouseY < headerHeight) {
			if (width - closeButtonWidth <= mouseX) {
				p.getEditorManager().closeFileChooser();
			}
		} else if (isInFileList(mouseX, mouseY)) {
			fileChooserList.mouseClicked(event, mouseX - fileChooserListMargin,
					mouseY - headerHeight - fileChooserListMargin);
		}
		return true;
	}

	@Override
	public boolean mouseMoved(MouseEvent event, float mouseX, float mouseY) {
		if (isInFileList(mouseX, mouseY)) {
			sendMouseMoved(fileChooserList, event,
					mouseX - fileChooserListMargin,
					mouseY - headerHeight - fileChooserListMargin);
		} else {
			fileChooserList.mouseExited(event);
		}
		if (width - closeButtonWidth <= mouseX && mouseY < headerHeight) {
			closeButtonMouseOver = true;
		} else {
			closeButtonMouseOver = false;
		}
		return true;
	}

	@Override
	public void mouseExited(MouseEvent event) {
		super.mouseExited(event);
		closeButtonMouseOver = false;
	}

	@Override
	public boolean mouseWheel(MouseEvent event, float mouseX, float mouseY) {
		if (isInFileList(mouseX, mouseY)) {
			fileChooserList.mouseWheel(event, mouseX - fileChooserListMargin,
					mouseY - headerHeight - fileChooserListMargin);
		}
		return true;
	}

	private boolean isInFileList(float mouseX, float mouseY) {
		return fileChooserListMargin <= mouseX
				&& mouseX < width - fileChooserListMargin
				&& headerHeight + fileChooserListMargin <= mouseY
				&& mouseY < height - fileChooserListMargin;
	}

	private static void setPath(Path path) {
		currentPath = Optional.ofNullable(path);
		childPaths.clear();
		if (currentPath.isPresent()) {
			try {
				path = path.toRealPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			childPaths.add(path.getParent());
			try (DirectoryStream<Path> stream = Files
					.newDirectoryStream(path)) {
				stream.forEach(childPath -> {
					childPaths.add(childPath);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			FileSystems.getDefault().getRootDirectories()
					.forEach(childPaths::add);
		}
	}

	private class FileChooserList extends Container<MainPApplet> {
		public FileChooserList(MainPApplet p) {
			super(p);
			scrollBar = new ScrollBarContainer(p, this, false);
			initGUI();
		}

		private float contentsHeight = 15;

		private int mouseOverIndex = -1;
		private int selectedIndex = -1;
		private ScrollBarContainer scrollBar;

		private float y;

		private void initGUI() {
			mouseOverIndex = -1;
			selectedIndex = -1;
			scrollBar.setFullValue(0, contentsHeight * childPaths.size());
			scrollBar.setDisplayValue(0);
		}

		@Override
		protected void draw() {
			scrollBar.setFullValue(0, contentsHeight * childPaths.size());
			scrollBar.setDisplayValueDiff(height);

			backgroundArea(p.color(255));
			clipArea();

			if (mouseOverIndex >= 0) {
				p.noStroke();
				p.fill(229, 243, 255);
				contentsRect(mouseOverIndex);
			}
			if (selectedIndex >= 0) {
				p.strokeWeight(1);
				p.stroke(152, 209, 255);
				p.fill(204, 232, 255);
				contentsRect(selectedIndex);
			}

			y = -scrollY();
			for (int i = 0; i < childPaths.size(); i++) {
				p.fill(0);
				p.textAlign(LEFT);
				p.textFont(p.getUiGothic(), contentsHeight * 0.7f);
				p.text(getChildPathName(i), 10, y + contentsHeight * 0.8f);
				y += contentsHeight;
			}

			if (openingFile) {
				p.noStroke();
				p.fill(0, 128);
				p.rect(0, 0, width, height);
				p.fill(0);
				p.textFont(p.getUiGothic(), 30);
				p.textAlign(CENTER, CENTER);
				p.text("opening...", width / 2, height / 2);
			}

			scrollBar.redrawToDefaultPlace(0, height);
		}

		private String getChildPathName(int i) {
			if (!currentPath.isPresent()) {
				Path name = childPaths.get(i).getRoot();
				return name.toString();
			} else if (i == 0) {
				return "上へ";
			} else {
				Path name = childPaths.get(i).getFileName();
				return name.toString();
			}
		}

		private void contentsRect(int index) {
			p.rect(5, contentsHeight * index - scrollY(), width,
					contentsHeight);
		}

		private float scrollY() {
			return scrollBar.getDisplayLow().floatValue();
		}

		@Override
		public void forEachChildContainer(
				Consumer<Container<?>> containerConsumer) {
			containerConsumer.accept(scrollBar);
		}

		@Override
		public boolean mousePressed(MouseEvent event, float mouseX,
				float mouseY) {
			if (openingFile) return false;
			if (super.mousePressed(event, mouseX, mouseY)) return true;
			return true;
		}

		@Override
		public boolean mouseWheel(MouseEvent event, float mouseX,
				float mouseY) {
			if (openingFile) return false;
			if (super.mouseWheel(event, mouseX, mouseY)) return true;
			scrollBar.setDisplayValue(
					s -> s + event.getCount() * contentsHeight * 1.1f);
			return true;
		}

		@Override
		public boolean mouseMoved(MouseEvent event, float mouseX,
				float mouseY) {
			if (openingFile) return false;
			if (super.mouseMoved(event, mouseX, mouseY)) {
				mouseExited(event);
				return true;
			}
			int index = getIndexFromMouseY(mouseY);
			mouseOverIndex = index;
			return true;
		}

		@Override
		public boolean mouseClicked(MouseEvent event, float mouseX,
				float mouseY) {
			if (openingFile) return false;
			if (super.mouseClicked(event, mouseX, mouseY)) return true;
			int index = getIndexFromMouseY(mouseY);
			if (event.getCount() % 2 == 1) {
				selectedIndex = index;
			} else if (event.getCount() % 2 == 0) {
				openFile(index);
			}
			return true;
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (!super.keyPressed(event)) {
				if (event.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
					requestSelectMove(selectedIndex - 1);
					return true;
				} else if (event
						.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
					requestSelectMove(selectedIndex + 1);
					return true;
				} else if (event
						.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
					openFile(selectedIndex);
				}
				return false;
			}
			return true;
		}

		private void requestSelectMove(int index) {
			if (index < 0) {
				index = 0;
			}
			if (index >= childPaths.size()) {
				index = childPaths.size() - 1;
			}
			selectedIndex = index;
			float diff = Math.min(contentsHeight * selectedIndex - scrollY(), 0)
					+ Math.max(contentsHeight * (selectedIndex + 1)
							- (scrollY() + height), 0);
			scrollBar.setDisplayValue(x -> x + diff);
		}

		private boolean openFile(int index) {
			if (index < 0 || childPaths.size() <= index) return false;
			Path path = childPaths.get(index);
			if (path == null || Files.isDirectory(path)) {
				setPath(path);
				initGUI();
			} else {
				openingFile = true;
				new Thread(() -> {
					if (pathOperation.test(path)) {
						p.getEditorManager().closeFileChooser();
					}
					openingFile = false;
				}).start();
			}
			return true;
		}

		private int getIndexFromMouseY(float mouseY) {
			int index = (int) ((mouseY + scrollY()) / contentsHeight);
			if (0 <= index && index < childPaths.size()) {
				return index;
			}
			return -1;
		}

		@Override
		public void mouseExited(MouseEvent event) {
			super.mouseExited(event);
			mouseOverIndex = -1;
		}
	}

	public static Optional<Path> getCurrentPath() {
		return currentPath;
	}
}
