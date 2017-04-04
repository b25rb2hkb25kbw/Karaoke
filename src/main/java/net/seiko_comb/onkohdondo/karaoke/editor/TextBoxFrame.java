package net.seiko_comb.onkohdondo.karaoke.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import processing.core.PApplet;
import processing.core.PFont;

@SuppressWarnings("serial")
public class TextBoxFrame extends JFrame
		implements WindowFocusListener, CaretListener, KeyListener {
	public static void main(String[] args) {
		TextBoxFrame textBoxFrame = new TextBoxFrame(null);
		textBoxFrame.setVisible(true);
	}

	private JTextComponent field;
	private String defaultString = "";
	private boolean finished = false;

	private Point point = new Point();
	private Optional<Runnable> closedOperation = Optional.empty();
	private Optional<Consumer<String>> changedOperation = Optional.empty();
	private Optional<Consumer<String>> canceledOperation = Optional.empty();
	private Optional<Predicate<String>> checkOperation = Optional.empty();

	public TextBoxFrame(PApplet p) throws HeadlessException {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(640, 480);
		setUndecorated(true);

		field = new JTextField("");
		field.addCaretListener(this);
		field.addKeyListener(this);
		add(field);
		adjustSize();

		if (p != null) {
			Component component = (Component) p.getSurface().getNative();
			point = component.getLocationOnScreen();
			// setLocationRelativeTo(component);
		}
		addWindowFocusListener(this);
	}

	private void adjustSize() {
		Dimension dim = field.getPreferredScrollableViewportSize();
		int width = (int) dim.getWidth();
		int height = (int) dim.getHeight();
		if (width < 10) width = 10;
		setSize(width + 2, height);
	}

	private void cancel() {
		if (finished) return;
		finished = true;
		canceledOperation.ifPresent(o -> o.accept(defaultString));
		close();
	}

	private void change() {
		if (finished) return;
		if (!verified) {
			cancel();
			return;
		}
		finished = true;
		changedOperation.ifPresent(a -> a.accept(field.getText()));
		close();
	}

	private void close() {
		dispose();
		closedOperation.ifPresent(Runnable::run);
	}

	private boolean verified = true;

	@Override
	public void caretUpdate(CaretEvent e) {
		adjustSize();
		verified = checkOperation.map(x -> x.test(field.getText()))
				.orElse(true);
		field.setBackground(verified ? Color.WHITE : Color.PINK);
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		cancel();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			change();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	public TextBoxFrame showArea() {
		setVisible(true);
		return this;
	}

	public TextBoxFrame setDefaultString(String defaultString) {
		this.defaultString = defaultString;
		field.setText(defaultString);
		field.selectAll();
		adjustSize();
		return this;
	}

	public TextBoxFrame setBounds(float x, float y) {
		point.translate((int) x, (int) y);
		point.translate(0, -getHeight() / 2);
		setLocation(point);
		return this;
	}

	public TextBoxFrame setTextFont(PFont pFont, float size) {
		Font font = (Font) pFont.getNative();
		field.setFont(font.deriveFont(size));
		return this;
	}

	public TextBoxFrame whenClosed(Runnable r) {
		closedOperation = Optional.ofNullable(r);
		return this;
	}

	public TextBoxFrame whenChanged(Consumer<String> r) {
		changedOperation = Optional.ofNullable(r);
		return this;
	}

	public TextBoxFrame whenCanceled(Consumer<String> r) {
		canceledOperation = Optional.ofNullable(r);
		return this;
	}

	public TextBoxFrame setChecker(Predicate<String> checker) {
		checkOperation = Optional.ofNullable(checker);
		return this;
	}
}
