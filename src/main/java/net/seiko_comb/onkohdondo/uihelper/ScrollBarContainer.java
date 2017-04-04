package net.seiko_comb.onkohdondo.uihelper;

import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import processing.event.MouseEvent;

public class ScrollBarContainer extends Container<ContainerPApplet> {

	private Container<? extends ContainerPApplet> parent;
	private boolean horizontal;

	public ScrollBarContainer(ContainerPApplet p,
			Container<? extends ContainerPApplet> parent, boolean horizontal) {
		super(p);
		this.parent = parent;
		this.horizontal = horizontal;

		setFullValue(0, 1);
		setDisplayValue(0, 0.1);
	}

	private float size = 18;
	private double fullLow;
	private double fullHigh;
	private double displayLow;
	private double displayHigh;
	private float startY;
	private float endY;

	public void redrawToDefaultPlace(float y, float height) {
		if (horizontal) {
			redraw(y, parent.height - size, height, size);
		} else {
			redraw(parent.width - size, y, size, height);
		}
	}

	@Override
	protected void draw() {
		p.pushMatrix();
		swapVariables(() -> {
			adjustStartEndY();
			if (horizontal) {
				p.rotate(-PI / 2);
				p.scale(-1, 1);
			}
			backgroundArea(p.color(240));

			p.noStroke();
			p.fill(166);
			p.rect(1, startY + 1, size - 2, endY - startY - 2);

			p.noStroke();
			p.fill(218);
			p.rect(0, 0, size, size);
			p.rect(0, height, size, -size);

			if (backClickCount >= 0) {
				if (backClickCount == 0
						|| (backClickCount >= 12 && backClickCount % 3 == 0)) {
					if (mouseY < startY) {
						setDisplayValue(displayLow - lowHigh());
					} else if (endY <= mouseY) {
						setDisplayValue(displayLow + lowHigh());
					}
				}
				backClickCount++;
			}
		});
		p.popMatrix();
	}

	public double lowHigh() {
		return displayHigh - displayLow;
	}

	private float dragStartStartY;
	private int backClickCount = -1;
	private boolean draggingBar = false;

	@Override
	public boolean mousePressed(MouseEvent event, float mx, float my) {
		super.mousePressed(event, mx, my);
		this.dragStartStartY = startY;
		swapVariables(mx, my, (mouseX, mouseY) -> {
			if (startY <= mouseY && mouseY < endY) {
				draggingBar = true;
			} else if (size <= mouseY && mouseY < height - size) {
				backClickCount = 0;
			}
		});
		return true;
	}

	@Override
	public boolean mouseReleased(MouseEvent event, float mx, float my) {
		super.mouseDropped(event, mx, my);
		backClickCount = -1;
		draggingBar = false;
		return true;
	}

	@Override
	public boolean mouseClicked(MouseEvent event, float mx, float my) {
		super.mouseClicked(event, mx, my);
		swapVariables(mx, my, (mouseX, mouseY) -> {
			if (mouseY < size) {
				setDisplayValue(displayLow - lowHigh() / 5);
			} else if (height - size <= mouseY) {
				setDisplayValue(displayLow + lowHigh() / 5);
			}
		});
		return true;
	}

	@Override
	public boolean mouseDragged(MouseEvent event, float mx, float my) {
		super.mouseDragged(event, mx, my);
		swapVariables(mx, my, (mouseX, mouseY) -> {
			if (draggingBar) {
				float diffY = mouseY - dragStartMouseY;
				double afterDisplayLow = getValue(dragStartStartY + diffY);
				setDisplayValue(afterDisplayLow);
			}
		});
		return true;
	}

	private float getY(double value) {
		return (float) (double) swapVariables(() -> {
			return (value - fullLow) / getFullHighLow() * (height - size * 2)
					+ size;
		});
	}

	private double getValue(float y) {
		return (y - size) / (height - size * 2) * getFullHighLow() + fullLow;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	// Change values ///////////////////////////////////////////////////////////

	public void setDisplayValue(double low, double high) {
		this.displayLow = low;
		this.displayHigh = high;
		adjustLowHigh();
		adjustStartEndY();
	}

	public void setDisplayValue(double displayLow) {
		setDisplayValueWithDiff(displayLow, lowHigh());
	}

	public void setDisplayValue(DoubleUnaryOperator displayLow) {
		setDisplayValue(displayLow.applyAsDouble(this.displayLow));
	}

	public void setDisplayValueDiff(double lowHigh) {
		setDisplayValueWithDiff(displayLow, lowHigh);
	}

	public void setDisplayValueDiff(DoubleUnaryOperator lowHigh) {
		setDisplayValueWithDiff(displayLow, lowHigh.applyAsDouble(this.lowHigh()));
	}

	private void setDisplayValueWithDiff(double start, double lowHigh) {
		if (getFullHighLow() < lowHigh) {
			start = fullLow;
		} else if (start < fullLow) {
			start = fullLow;
		} else if (fullHigh <= start + lowHigh) {
			start = fullHigh - lowHigh;
		}
		displayLow = start;
		displayHigh = start + lowHigh;
		adjustStartEndY();
	}

	private double getFullHighLow() {
		return fullHigh - fullLow;
	}

	public void setFullValue(double low, double high) {
		this.fullLow = low;
		this.fullHigh = high;
		adjustLowHigh();
		adjustStartEndY();
	}

	public Number getDisplayLow() {
		return displayLow;
	}

	public Number getDisplayHigh() {
		return displayHigh;
	}

	private void adjustLowHigh() {
	}

	private void adjustStartEndY() {
		this.startY = getY(Math.max(fullLow, displayLow));
		this.endY = getY(Math.min(fullHigh, displayHigh));
	}

	// Swap variables //////////////////////////////////////////////////////////

	private int swapCount = 0;

	private void swapVariables(boolean start) {
		if (horizontal) {
			if (start) {
				swapCount++;
			} else {
				swapCount--;
			}
			if ((start && swapCount == 1) || (!start && swapCount == 0)) {
				float buf = width;
				width = height;
				height = buf;
				buf = mouseX;
				mouseX = mouseY;
				mouseY = buf;
				buf = dragStartMouseX;
				dragStartMouseX = dragStartMouseY;
				dragStartMouseY = buf;
			}
		}
	}

	private void swapVariables(Runnable r) {
		swapVariables(true);
		r.run();
		swapVariables(false);
	}

	private <T> T swapVariables(Supplier<T> s) {
		swapVariables(true);
		T t = s.get();
		swapVariables(false);
		return t;
	}

	private void swapVariables(float mouseX, float mouseY,
			BiConsumer<Float, Float> c) {
		swapVariables(true);
		if (horizontal)
			c.accept(mouseY, mouseX);
		else c.accept(mouseX, mouseY);
		swapVariables(false);
	}

}
