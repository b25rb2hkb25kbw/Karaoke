package net.seiko_comb.onkohdondo.karaoke.editor;

import java.util.function.Consumer;

import net.seiko_comb.onkohdondo.karaoke.editor.data.SoundTrack;
import net.seiko_comb.onkohdondo.uihelper.Container;
import processing.event.MouseEvent;

public class SoundTrackLabelContainer extends TrackLabelContainer {

	private SoundTrack soundTrack;

	public SoundTrackLabelContainer(MainPApplet p, SoundTrack soundTrack) {
		super(p);
		switcher = new SpectrogramSwitcher(p);
		this.soundTrack = soundTrack;
	}

	private SpectrogramSwitcher switcher;

	@Override
	protected void draw() {
		super.draw();
		p.strokeWeight(1);
		p.stroke(0);
		p.fill(255, 255, 232);
		p.rect(0, headerHeight, width, height - headerHeight);

		switcher.redraw(0, headerHeight, width, 30);
	}

	@Override
	public void forEachChildContainer(Consumer<Container<?>> c) {
		c.accept(switcher);
	}

	private class SpectrogramSwitcher extends Container<MainPApplet> {
		public SpectrogramSwitcher(MainPApplet p) {
			super(p);
		}

		@Override
		protected void draw() {
			backgroundArea(p.color(128));

			p.fill(12);
			p.textSize(12);
			p.textAlign(CENTER);
			p.text("Display Power Graph", width / 2, (height + 12) / 2);
		}

		@Override
		public boolean mouseClicked(MouseEvent event, float mouseX,
				float mouseY) {
			if (!super.mouseClicked(event, mouseX, mouseY)) {
				p.getEditorManager().addPowerViewerTrack(soundTrack);
			}
			return false;
		}
	}
}
