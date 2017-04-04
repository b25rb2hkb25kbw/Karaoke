package net.seiko_comb.onkohdondo.karaoke.data;

import processing.core.PApplet;

public class SoundFileViewer extends PApplet {

	public static void main(String[] args) {
		PApplet.main(SoundFileViewer.class.getName());
	}

	public void settings() {
		size(900, 400);
	}

	public void setup() {
	}

	public void draw() {
	}

	public float map(double a, double b, double c, double d, double e) {
		return map((float) a, (float) b, (float) c, (float) d, (float) e);
	}
}
