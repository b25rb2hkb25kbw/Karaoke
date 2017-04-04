package net.seiko_comb.onkohdondo.uihelper.test;

import net.seiko_comb.onkohdondo.uihelper.Container;
import processing.core.PConstants;
import processing.event.MouseEvent;

public class LeftViewer extends Container<UIHelperTest> {

	public LeftViewer(UIHelperTest p) {
		super(p);
	}

	private float contentsHeight = 20;
	private String[] contents = { "Apple", "Banana", "Chocolate" };
	private int mouseOver = -1;

	@Override
	protected void draw() {
		backgroundArea(p.color(255));
		for (int i = 0; i < contents.length; i++) {
			if (i == mouseOver) {
				p.strokeWeight(1);
				p.stroke(72, 72, 255);
				p.fill(220, 220, 255);
				p.rect(-10, contentsHeight * (i + 0.05f), width + 20,
						contentsHeight * 0.9f);
			}
			p.fill(0);
			p.textSize(contentsHeight * 0.7f);
			p.textAlign(PConstants.LEFT);
			p.text(contents[i], 10, contentsHeight * (i + 0.8f));
		}
	}

	@Override
	public boolean mouseMoved(MouseEvent event, float mouseX, float mouseY) {
		mouseOver = (int) (mouseY / contentsHeight);
		return true;
	}

	@Override
	public void mouseExited(MouseEvent event) {
		mouseOver = -1;
	}
}
