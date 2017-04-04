package net.seiko_comb.onkohdondo.uihelper.test;

import net.seiko_comb.onkohdondo.uihelper.Container;
import processing.core.PConstants;

public class RightViewer extends Container<UIHelperTest> {

	public RightViewer(UIHelperTest p) {
		super(p);
	}
	
	private String text = "N/A";

	public void draw() {
		backgroundArea(p.color(204));
		p.fill(0);
		p.textSize(40);
		p.textAlign(PConstants.CENTER);
		p.text(text, width/2, height/2-20);
	}

}
