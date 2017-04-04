package net.seiko_comb.onkohdondo.uihelper;

import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class ContainerPApplet extends PApplet {

	protected abstract Container<? extends ContainerPApplet> getTopLevelContainer();

	public void draw() {
		Container<? extends ContainerPApplet> base = getTopLevelContainer();
		base.redraw(0, 0, width, height);
	}

	@Override
	protected void handleKeyEvent(KeyEvent event) {
		super.handleKeyEvent(event);
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		getTopLevelContainer().mouseMoved(event, mouseX, mouseY);
	}

	@Override
	public void mouseExited(MouseEvent event) {
		getTopLevelContainer().mouseExited(event);
	}

	@Override
	public void mousePressed(MouseEvent event) {
		getTopLevelContainer().mousePressed(event, mouseX, mouseY);
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		getTopLevelContainer().mouseDragged(event, mouseX, mouseY);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		getTopLevelContainer().mouseReleased(event, mouseX, mouseY);
		getTopLevelContainer().mouseDropped(event, mouseX, mouseY);
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		getTopLevelContainer().mouseClicked(event, mouseX, mouseY);
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		getTopLevelContainer().mouseWheel(event, mouseX, mouseY);
	}

	@Override
	public void keyPressed(KeyEvent event) {
		getTopLevelContainer().keyPressed(event);
	}

	@Override
	public void keyReleased(KeyEvent event) {
		getTopLevelContainer().keyReleased(event);
	}

	@Override
	public void keyTyped(KeyEvent event) {
		getTopLevelContainer().keyTyped(event);
	}

	public static String getPlace(StackTraceElement elem) {
		boolean nativeMethod = elem.isNativeMethod();
		String fileName = elem.getFileName();
		int lineNumber = elem.getLineNumber();
		return (nativeMethod ? "(Native Method)"
				: (fileName != null && lineNumber >= 0
						? "(" + fileName + ":" + lineNumber + ")"
						: (fileName != null ? "(" + fileName + ")"
								: "(Unknown Source)")));
	}
}
