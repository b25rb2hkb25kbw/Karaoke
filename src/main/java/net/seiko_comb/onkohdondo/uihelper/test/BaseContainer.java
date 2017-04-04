package net.seiko_comb.onkohdondo.uihelper.test;

import java.util.Optional;

import net.seiko_comb.onkohdondo.uihelper.Container;
import processing.event.Event;
import processing.event.MouseEvent;

public class BaseContainer extends Container<UIHelperTest> {
	public BaseContainer(UIHelperTest p) {
		super(p);

		left = new LeftViewer(p);
		right = new RightViewer(p);
	}

	private float leftWidth = 300;

	private LeftViewer left;
	private RightViewer right;

	@Override
	public void draw() {
		left.redraw(0, 0, leftWidth, height);
		right.redraw(leftWidth, 0, width - leftWidth, height);

		p.stroke(0);
		p.strokeWeight(3);
		p.line(leftWidth, 0, leftWidth, height);

		dragX.ifPresent(x -> {
			p.stroke(0, 128);
			p.strokeWeight(3);
			p.line(leftWidth + x, 0, leftWidth + x, height);
		});
	}

	@Override
	public boolean mouseMoved(MouseEvent event, float mouseX, float mouseY) {
		if (Math.abs(mouseX - leftWidth) < 3) {
		} else if (mouseX < leftWidth) {
			sendMouseMoved(left, event, mouseX, mouseY);
		} else {
			sendMouseMoved(right, event, mouseX - leftWidth, mouseY);
		}
		return true;
	}

	private Optional<Float> dragX = Optional.empty();

	@Override
	public boolean mouseDragged(MouseEvent event, float mouseX, float mouseY) {
		if (Math.abs(dragStartMouseX - leftWidth) < 3) {
			float diff = mouseX - dragStartMouseX;
			if ((event.getModifiers() & Event.SHIFT) > 0) {
				dragX = Optional.of(diff / 2);
			} else {
				dragX = Optional.of(diff);
			}
		}
		return true;
	}

	@Override
	public boolean mouseDropped(MouseEvent event, float mouseX, float mouseY) {
		if (dragX.isPresent()) {
			leftWidth += dragX.get();
			dragX = Optional.empty();
		}
		return true;
	}
}
