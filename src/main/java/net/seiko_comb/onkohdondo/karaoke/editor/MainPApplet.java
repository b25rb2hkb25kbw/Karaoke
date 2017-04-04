package net.seiko_comb.onkohdondo.karaoke.editor;

import net.seiko_comb.onkohdondo.uihelper.ContainerPApplet;
import processing.core.PApplet;
import processing.core.PFont;

public class MainPApplet extends ContainerPApplet {
	public static void main(String[] args) {
		PApplet.main(MainPApplet.class.getName());
	}

	private EditorManager editorManager;
	private BaseContainer base;

	@Override
	protected BaseContainer getTopLevelContainer() {
		return base;
	}

	protected BaseContainer getBaseContainer() {
		return base;
	}

	@Override
	public void settings() {
		size(960, 540);
		// noSmooth();
	}

	private PFont uiGothic;

	public void setup() {
		surface.setResizable(true);
		editorManager = new EditorManager(this);
		base = new BaseContainer(this);
		uiGothic = createFont("Yu Gothic UI Regular", 100);
	}

	public void draw() {
		super.draw();
	}

	public EditorManager getEditorManager() {
		return editorManager;
	}

	public PFont getUiGothic() {
		return uiGothic;
	}
}
