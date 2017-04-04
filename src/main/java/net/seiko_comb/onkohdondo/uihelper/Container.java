package net.seiko_comb.onkohdondo.uihelper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class Container<T extends ContainerPApplet> implements PConstants {
	protected T p;

	public Container(T p) {
		this.p = p;
	}

	protected float x, y;
	protected float width, height;
	private float drawX = 0, drawY = 0, drawWidth = this.width,
			drawHeight = this.height;
	private boolean clipped = false;

	public final void redraw(float x, float y, float width, float height) {
		p.pushMatrix();

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		p.translate(x, y);

		initDrawCoordinates();
		clipped = false;

		draw();

		if (clipped) {
			p.noClip();
		}

		p.popMatrix();
	}

	protected void initDrawCoordinates() {
		drawX = 0;
		drawY = 0;
		drawWidth = this.width;
		drawHeight = this.height;
	}

	protected void redrawToLeft(float width, Container<?> subContainer) {
		subContainer.redraw(drawX, drawY, width, drawHeight);
		drawX += width;
		drawWidth -= width;
	}

	protected void redrawToCenter(Container<?> container) {
		container.redraw(drawX, drawY, drawWidth, drawHeight);
	}

	protected void draw() {
	}

	public void forEachChildContainer(Consumer<Container<?>> c) {

	}

	protected float mouseX, mouseY;

	public boolean mouseMoved(MouseEvent event, float mouseX, float mouseY) {
		boolean[] consumed = { false };
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.mouseIn = true;
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(mouseX, mouseY)) {
				consumed[0] = sendMouseMoved(container, event,
						mouseX - container.x, mouseY - container.y);
			}
		});
		return consumed[0];
	}

	protected Optional<Container<?>> beforeMouseMoved = Optional.empty();

	public boolean sendMouseMoved(Container<?> container, MouseEvent event,
			float mouseX, float mouseY) {
		if (!beforeMouseMoved.map(container::equals).orElse(false)) {
			beforeMouseMoved.ifPresent(c -> c.mouseExited(event));
			beforeMouseMoved = Optional.of(container);
		}
		return container.mouseMoved(event, mouseX, mouseY);
	}

	private boolean mouseIn = false;

	public void mouseExited(MouseEvent event) {
		this.mouseIn = false;
		beforeMouseMoved.ifPresent(c -> c.mouseExited(event));
		beforeMouseMoved = Optional.empty();
	}

	protected float dragStartMouseX, dragStartMouseY;

	public boolean mousePressed(MouseEvent event, float mouseX, float mouseY) {
		dragStartMouseX = mouseX;
		dragStartMouseY = mouseY;
		boolean[] consumed = { false };
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(mouseX, mouseY)) {
				consumed[0] = container.mousePressed(event,
						mouseX - container.x, mouseY - container.y);
			}
		});
		return consumed[0];
	}

	public boolean mouseDragged(MouseEvent event, float mouseX, float mouseY) {
		boolean[] consumed = { false };
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(dragStartMouseX, dragStartMouseY)) {
				consumed[0] = container.mouseDragged(event,
						mouseX - container.x, mouseY - container.y);
			}
		});
		return consumed[0];
	}

	public boolean mouseReleased(MouseEvent event, float mouseX, float mouseY) {
		boolean[] consumed = { false };
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(dragStartMouseX, dragStartMouseY)) {
				consumed[0] = container.mouseReleased(event,
						mouseX - container.x, mouseY - container.y);
			}
		});
		return consumed[0];
	}

	public boolean mouseDropped(MouseEvent event, float mouseX, float mouseY) {
		boolean[] consumed = { false };
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(mouseX, mouseY)) {
				consumed[0] = container.mouseDropped(event,
						mouseX - container.x, mouseY - container.y);
			}
		});
		return consumed[0];
	}

	protected Optional<Container<?>> lastMouseClicked = Optional.empty();

	public boolean mouseClicked(MouseEvent event, float mouseX, float mouseY) {
		boolean[] consumed = { false };
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(mouseX, mouseY)) {
				consumed[0] = container.mouseClicked(event,
						mouseX - container.x, mouseY - container.y);
				lastMouseClicked = Optional.of(container);
			}
		});
		return consumed[0];
	}

	public boolean mouseWheel(MouseEvent event, float mouseX, float mouseY) {
		boolean[] consumed = { false };
		forEachChildContainer(container -> {
			if (consumed[0]) return;
			if (container == null) return;
			if (container.pointIn(mouseX, mouseY)) {
				consumed[0] = container.mouseWheel(event, mouseX - container.x,
						mouseY - container.y);
			}
		});
		return consumed[0];
	}

	/**
	 * Event occurs if key is pressed. The event is first sent to the Container
	 * which was clicked most recently. After that, if the event is not
	 * consumed, the event is sent to the other Containers.
	 * 
	 * When you add the listener implementation of this method, use the idiom
	 * following: <br>
	 * <code>
	 * public boolean keyPressed(KeyEvent event){ 
	 * 	if(!super.keyPressed(event)){
	 * 		// Your implementation goes here.
	 * 		return true; // if the event was consumed.
	 * 		// otherwise
	 * 		// return false; // if the event was not consumed.
	 *  }
	 *  return true;
	 * }
	 * </code>
	 * 
	 * @param event
	 *            the event sent from Processing platform.
	 * @return If the event is consumed (i.e. if the event affects the process
	 *         of the program), this method must return true. This is because
	 *         one key event is mapped no more than one operation. Otherwise,
	 *         this method returns false.
	 */
	public boolean keyPressed(KeyEvent event) {
		return performKeyEvent(c -> c.keyPressed(event));
	}

	/**
	 * 
	 * @param event
	 * @return
	 * 
	 * @see net.seiko_comb.onkohdondo.uihelper.Container#keyPressed(KeyEvent)
	 */
	public boolean keyReleased(KeyEvent event) {
		return performKeyEvent(c -> c.keyReleased(event));
	}

	/**
	 * 
	 * @param event
	 * @return
	 * 
	 * @see net.seiko_comb.onkohdondo.uihelper.Container#keyPressed(KeyEvent)
	 */
	public boolean keyTyped(KeyEvent event) {
		return performKeyEvent(c -> c.keyTyped(event));
	}

	private boolean performKeyEvent(Predicate<Container<?>> eventMethod) {
		if (lastMouseClicked
				.map(clickedContainer -> eventMethod.test(clickedContainer))
				.orElse(false)) {
			return true;
		}
		Container<?> c = lastMouseClicked.orElse(null);
		boolean[] consumed = { false };
		forEachChildContainer(childContainer -> {
			if (consumed[0]) return;
			if (childContainer != c) {
				consumed[0] = eventMethod.test(childContainer);
			}
		});
		return consumed[0];
	}

	protected void backgroundArea(int color) {
		p.noStroke();
		p.fill(color);
		p.rect(0, 0, width, height);
	}

	protected void clipArea() {
		p.clip(0, 0, width, height);
		clipped = true;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public boolean mouseIsIn() {
		return mouseIn;
	}

	public boolean pointIn(float px, float py) {
		return x <= px && px < x + width && y <= py && py < y + height;
	}
}
