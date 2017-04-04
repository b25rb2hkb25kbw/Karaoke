package net.seiko_comb.onkohdondo.karaoke.editor.old;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class ResizableCanvas extends Canvas {

	public ResizableCanvas() {
		// Redraw canvas when size changes.
		widthProperty().addListener(evt -> draw());
		heightProperty().addListener(evt -> draw());
	}

	private void draw() {
		double width = getWidth();
		double height = getHeight();

		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, width, height);

		gc.setStroke(Color.RED);
		gc.strokeLine(0, 0, width, height);
		gc.strokeLine(0, height, width, 0);
	}

	@Override
	public boolean isResizable() {
		return true;
	}
}