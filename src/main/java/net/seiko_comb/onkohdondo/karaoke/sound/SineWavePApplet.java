package net.seiko_comb.onkohdondo.karaoke.sound;

import java.awt.event.KeyEvent;

import net.seiko_comb.onkohdondo.karaoke.editor.output.FrequencyOutputControler;
import net.seiko_comb.onkohdondo.karaoke.editor.output.OutputManager;
import processing.core.PApplet;

@SuppressWarnings("unused")
public class SineWavePApplet extends PApplet {

	public static void main(String[] args) {
		PApplet.main(SineWavePApplet.class.getName());
	}

	@Override
	public void settings() {
		size(640, 180);
	}

	private OutputManager out;
	private FrequencyOutputControler controler;

	private double pitch = 60;

	private char[] keys = { 'a', 'w', 's', 'e', 'd', 'r', 'f', 'g', 'y', 'h',
			'u', 'j', 'k', 'o', 'l', 'p', ';', '@', ':', ']', };
	private String[] names = { "C", "Cis-Des", "D", "Dis-Es", "E", "F",
			"Fis-Ges", "G", "Gis-As", "A", "Ais-B", "B", };

	public void setup() {
		out = new OutputManager();
		controler = new FrequencyOutputControler(
				x -> Math.sin(x * 2 * Math.PI));
		controler.setVelocity(0.02);
		out.addOutputControler(controler);
	}

	private boolean answer = false;

	public void draw() {
		background(255);
		if (answer) {
			textSize(30);
			textAlign(CENTER, CENTER);
			fill(0);
			int pi = (int) Math.round(pitch);
			String name = names[pi % 12];
			if (pi == pitch) {
				text(name, width / 2, height / 2);
			} else {
				String to;
				if (pitch > pi) {
					to = names[(pi + 1) % 12];
				} else {
					to = names[(pi + 11) % 12];
				}
				text(String.format("%s->%s (%+f)", name, to, pitch - pi),
						width / 2, height / 2);
			}
		}
	}

	public void keyPressed() {
//		for (int i = 0; i < keys.length; i++) {
//			if (keyCode == KeyEvent.getExtendedKeyCodeForChar(keys[i])) {
//				controler.setFrequency(getFrequencyFromNote(53 + i));
//				controler.setVelocity(0.10);
//			}
//		}
	}

	public void keyReleased() {
//		controler.setVelocity(0);
	}

	private int res = 2;

	public void keyTyped() {
		if (key == ' ') {
			pitch = (int) (random(60, 72) * res) / (double) res;
			controler.setFrequency(getFrequencyFromNote(pitch));
			answer = false;
		} else if (key == 'a') {
			answer = true;
			System.out.println(pitch);
		}
	}

	public double getFrequencyFromNote(double note) {
		return 440.0 * Math.pow(2, (note - 69) / 12);
	}
}
