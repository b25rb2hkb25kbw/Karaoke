package net.seiko_comb.onkohdondo.uihelper.test;

import net.seiko_comb.onkohdondo.uihelper.Container;
import net.seiko_comb.onkohdondo.uihelper.ContainerPApplet;
import processing.core.PApplet;

public class UIHelperTest extends ContainerPApplet {

	public static void main(String[] args) {
		PApplet.main(UIHelperTest.class.getName());
	}

	public void settings() {
		size(960, 540);
		noSmooth();
	}

	private BaseContainer base;

	public void setup() {
		surface.setResizable(true);

		base = new BaseContainer(this);
		textFont(createFont("MS UI Gothic", 100));
	}

	public void draw() {
		base.redraw(0, 0, width, height);
	}

	@Override
	protected Container<UIHelperTest> getTopLevelContainer() {
		return base;
	}
}
