package net.seiko_comb.onkohdondo.karaoke.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.seiko_comb.onkohdondo.karaoke.data.MeasureStake;
import net.seiko_comb.onkohdondo.karaoke.data.TempoStake;
import net.seiko_comb.onkohdondo.karaoke.data.TempoTrack;
import net.seiko_comb.onkohdondo.uihelper.Container;
import processing.event.MouseEvent;

public class TempoTrackContentsContainer extends TrackContentsContainer {

	private TempoTrack tempoTrack;

	private MeasureViewerContainer measureContainer;
	private BPMAdjusterContainer bpmContainer;

	public TempoTrackContentsContainer(MainPApplet p, TempoTrack track) {
		super(p);
		this.tempoTrack = track;

		this.measureContainer = new MeasureViewerContainer(p);
		this.bpmContainer = new BPMAdjusterContainer(p);
	}

	@Override
	protected void draw() {
		measureContainer.redraw(0, 0, width, height / 2);
		bpmContainer.redraw(0, height / 2, width, height / 2);
	}

	@Override
	public void forEachChildContainer(Consumer<Container<?>> c) {
		c.accept(measureContainer);
		c.accept(bpmContainer);
	}

	private float bpmLabelHeight = 15;

	/**
	 * 
	 * @param left
	 *            draw to the left side, or right side of the stake?
	 * @param strokeColor
	 * @param fillColor
	 * @param textColor
	 */
	private <T> Rectangle<T> drawLabel(float x, float y, String text,
			boolean left, int strokeColor, int fillColor, int textColor, T t) {
		final int margin = 3;
		float h = bpmLabelHeight;
		p.textSize(h * 0.8f);
		float w = p.textWidth(text) + 5;
		boolean arrow = false;
		x += margin;
		if (left) {
			x -= margin * 2 + w;
		}
		if (x < 0) {
			x = margin;
			arrow = true;
		}
		p.strokeWeight(1);
		p.stroke(strokeColor);
		p.fill(fillColor);
		if (arrow) {
			p.beginShape();
			p.vertex(x, y - h / 2);
			p.vertex(x + w, y - h / 2);
			p.vertex(x + w + 3, y);
			p.vertex(x + w, y + h / 2);
			p.vertex(x, y + h / 2);
			p.endShape(CLOSE);
		} else {
			p.rect(x, y - h / 2, w, h);
		}
		p.fill(textColor);
		p.textAlign(CENTER, CENTER);
		p.text(text, x + w / 2, y);
		return new Rectangle<>(x, y - h / 2, w, h, t);
	}

	private double getBeatOnX(float x) {
		double duration = 480;
		return Math.round(tempoTrack.getBeat(
				trackContainer.getTimelineContainer().getTimeOnPixelX(x))
				/ duration) * duration;
	}

	private OptionalDouble mouseOverBeat = OptionalDouble.empty();

	@Override
	public boolean mouseMoved(MouseEvent event, float mouseX, float mouseY) {
		if (super.mouseMoved(event, mouseX, mouseY)) return true;
		mouseOverBeat = OptionalDouble.of(getBeatOnX(mouseX));
		return true;
	}

	@Override
	public void mouseExited(MouseEvent event) {
		super.mouseExited(event);
		mouseOverBeat = OptionalDouble.empty();
	}

	private class MeasureViewerContainer extends Container<MainPApplet> {
		public MeasureViewerContainer(MainPApplet p) {
			super(p);
		}

		private List<Rectangle<MeasureStake>> labels = new ArrayList<>();

		@Override
		protected void draw() {
			clipArea();
			backgroundArea(p.color(255, 247, 192));
			double startBeat = tempoTrack.getBeat(
					trackContainer.getTimelineContainer().getTimeOnPixelX(0));
			double endBeat = tempoTrack.getBeat(trackContainer
					.getTimelineContainer().getTimeOnPixelX(width));
			boolean drawed = false;
			labels.clear();
			for (int i = 0; i < tempoTrack.getMeasureStakes().size(); i++) {
				MeasureStake stake = tempoTrack.getMeasureStakeAt(i);
				double beat = stake.getBeat();
				if (i < tempoTrack.getMeasureStakes().size() - 1
						&& beat < startBeat)
					continue;
				if (!drawed) {
					drawed = true;
					MeasureStake stakeBefore = tempoTrack
							.getMeasureStakeAt(i - 1);
					double measureLength = stakeBefore.getMeasureBeatLength();
					drawMeasureLine(
							beat - Math.ceil((beat - startBeat) / measureLength)
									* measureLength,
							Math.min(beat, endBeat), stakeBefore);
					drawMeasureLabel(stakeBefore);
				}
				p.stroke(255, 0, 0);
				beatLine(trackContainer.getTimelineContainer()
						.getPixelXOnTime(tempoTrack.getSecond(beat)));
				double beatEnd = i + 1 == tempoTrack.getMeasureStakes().size()
						? endBeat
						: tempoTrack.getMeasureStakeAt(i + 1).getBeat();
				drawMeasureLine(beat, beatEnd, stake);
				drawMeasureLabel(stake);
			}
		}

		private void drawMeasureLine(double beatStart, double beatEnd,
				MeasureStake measureStake) {
			double measureLength = measureStake.getMeasureBeatLength();
			double baseLength = measureStake.getBaseBeatLength();
			for (double beat = beatStart; beat < beatEnd; beat += measureLength) {
				if (beatStart < beat) {
					p.stroke(0, 255, 0);
					beatLine(trackContainer.getTimelineContainer()
							.getPixelXOnTime(tempoTrack.getSecond(beat)));
				}
				for (double littleBeat = beat + baseLength; littleBeat < beat
						+ measureLength - 1E-3; littleBeat += baseLength) {
					p.stroke(128);
					beatLine(trackContainer.getTimelineContainer()
							.getPixelXOnTime(tempoTrack.getSecond(littleBeat)));
				}
			}
		}

		private void drawMeasureLabel(MeasureStake measureStake) {
			if (editingStake.map(measureStake::equals).orElse(false)) return;
			float x = trackContainer.getTimelineContainer().getPixelXOnTime(
					tempoTrack.getSecond(measureStake.getBeat()));
			labels.add(drawLabel(x, height / 2, toText(measureStake), false,
					p.color(0), p.color(255), p.color(0), measureStake));
		}

		private String toText(MeasureStake stake) {
			return String.format("%d/%d", stake.getBeatCount(),
					stake.getBaseCount());
		}

		private void beatLine(float y) {
			p.line(y, 0, y, height);
		}

		private Optional<MeasureStake> editingStake = Optional.empty();

		@Override
		public boolean mousePressed(MouseEvent event, float mouseX,
				float mouseY) {
			if (super.mousePressed(event, mouseX, mouseY)) return true;

			// label clicked
			for (Rectangle<MeasureStake> rect : labels) {
				MeasureStake stake = rect.t;
				int beforeBaseCount = stake.getBaseCount(),
						beforeBeatCount = stake.getBeatCount();
				if (!rect.in(mouseX, mouseY)) continue;
				new TextBoxFrame(p).setDefaultString(toText(stake))
						.setBounds(event.getX() - mouseX + rect.x,
								event.getY() - mouseY + rect.y
										+ rect.height / 2)
						.setTextFont(p.getUiGothic(), rect.height * 0.8f)
						.setChecker(text -> parseMeasureValue(text, (a, b) -> {
							if (a <= 0 || b <= 0) return false;
							if (b != (b & -b)) return false;
							return true;
						}).orElse(false))
						.whenChanged(text -> parseMeasureValue(text, (a, b) -> {
							p.getEditorManager().editTrack(tempoTrack,
									t -> stake.setMeasureCounts(x -> a,
											x -> b));
						}))
						.whenCanceled(string -> stake.setMeasureCounts(
								a -> beforeBeatCount, a -> beforeBaseCount))
						.whenClosed(() -> editingStake = Optional.empty())
						.showArea();
				editingStake = Optional.of(stake);
				return true;
			}

			// stake clicked
			double beat = getBeatOnX(mouseX);
			Optional<MeasureStake> mouseStake = tempoTrack
					.getMeasureStakeAtBeat(beat);
			if (mouseStake.isPresent()) {
				if (event.getButton() == CENTER) {
					p.getEditorManager().editTrack(tempoTrack,
							t -> t.removeMeasureStake(beat));
				}
			} else {
				p.getEditorManager().editTrack(tempoTrack,
						t -> t.addMeasureStake(beat));
			}
			return false;
		}

		private final Pattern MEASURE_PATTERN = Pattern
				.compile("(\\d+)(/(\\d+))?");

		private void parseMeasureValue(String text,
				BiConsumer<Integer, Integer> consumer) {
			parseMeasureValue(text, (a, b) -> {
				consumer.accept(a, b);
				return null;
			});
		}

		private <T> Optional<T> parseMeasureValue(String text,
				BiFunction<Integer, Integer, T> function) {
			Matcher matcher = MEASURE_PATTERN.matcher(text);
			if (!matcher.matches()) return Optional.empty();
			int a = Integer.parseInt(matcher.group(1));
			int b = Optional.ofNullable(matcher.group(3)).map(Integer::parseInt)
					.orElse(4);
			return Optional.ofNullable(function.apply(a, b));
		}

	}

	private class BPMAdjusterContainer extends Container<MainPApplet> {
		public BPMAdjusterContainer(MainPApplet p) {
			super(p);
		}

		private List<Rectangle<Integer>> labels = new ArrayList<>();

		protected void draw() {
			clipArea();
			backgroundArea(p.color(192, 192, 255));
			// double startBeat = tempoTrack.getBeat(
			// trackContainer.getTimelineContainer().getTimeOnPixelX(0));
			// double endBeat = tempoTrack.getBeat(trackContainer
			// .getTimelineContainer().getTimeOnPixelX(width));
			// double duration = 480;
			// for (double beat = duration * Math.floor(
			// startBeat / duration); beat < endBeat; beat += duration) {
			// double second = tempoTrack.getSecond(beat);
			// float y = trackContainer.getTimelineContainer()
			// .getPixelXOnTime(second);
			// p.stroke(128);
			// beatLine(y);
			// }
			mouseOverBeat.ifPresent(beat -> {
				p.stroke(255, 128);
				beatLine(trackContainer.getTimelineContainer()
						.getPixelXOnTime(tempoTrack.getSecond(beat)));
			});
			labels.clear();
			for (int i = 0; i < tempoTrack.getStakes().size(); i++) {
				TempoStake stake = tempoTrack.getStakes().get(i);
				double second = stake.getSecond();
				float x = trackContainer.getTimelineContainer()
						.getPixelXOnTime(second);
				p.stroke(255, 32, 32);
				beatLine(x);
				if (i == 0) {
					drawBpmLabel(x, tempoTrack.getDefaultBpm(), true,
							tempoTrack.defaultTempoIsSet(), -1);
				}
				drawBpmLabel(x, tempoTrack.getBpmAt(i), false,
						i < tempoTrack.getStakes().size() - 1
								|| tempoTrack.afterTempoIsSet(),
						i);
			}
		}

		private void drawBpmLabel(float x, double bpm, boolean left,
				boolean specified, Integer index) {
			if (editingStake.map(index::equals).orElse(false)) return;
			labels.add(drawLabel(x, height / 2, formatBpm(bpm), left,
					p.color(0), specified ? p.color(255) : p.color(216),
					specified ? p.color(0) : p.color(128), index));
		}

		private String formatBpm(double bpm) {
			return String.format("%.3f", bpm);
		}

		private Optional<TempoStake> clickingStake = Optional.empty();
		private Optional<Integer> editingStake = Optional.empty();

		@Override
		public boolean mousePressed(MouseEvent event, float mouseX,
				float mouseY) {
			if (super.mousePressed(event, mouseX, mouseY)) return true;

			// If label is clicked
			for (ListIterator<Rectangle<Integer>> it = labels
					.listIterator(labels.size()); it.hasPrevious();) {
				Rectangle<Integer> rect = it.previous();
				if (!rect.in(mouseX, mouseY)) continue;
				int index = rect.t;
				DoubleConsumer[] bpmSetter = { null };
				double[] initialBpm = { tempoTrack.getBpmAt(index) };
				if (index == -1) {
					if (event.getButton() == CENTER) {
						p.getEditorManager().editTrack(tempoTrack,
								TempoTrack::removeBeforeTempo);
						return true;
					}
					if (!tempoTrack.defaultTempoIsSet()) initialBpm[0] = 0;
					bpmSetter[0] = tempoTrack::setDefaultBpm;
				} else if (index + 1 == tempoTrack.getStakes().size()) {
					if (event.getButton() == CENTER) {
						if (tempoTrack.afterTempoIsSet()) {
							p.getEditorManager().editTrack(tempoTrack,
									TempoTrack::removeAfterTempo);
						} else {
							p.getEditorManager().editTrack(tempoTrack,
									t -> t.removeStakeOnIndex(rect.t));
						}
						return true;
					}
					if (!tempoTrack.afterTempoIsSet()) initialBpm[0] = 0;
					bpmSetter[0] = tempoTrack::setAfterBpm;
				} else {
					if (event.getButton() != CENTER) continue;
					p.getEditorManager().editTrack(tempoTrack,
							t -> t.removeStakeOnIndex(rect.t));
					return true;
				}
				new TextBoxFrame(p)
						.setDefaultString(formatBpm(tempoTrack.getBpmAt(index)))
						.setBounds(event.getX() - mouseX + rect.x,
								event.getY() - mouseY + rect.y
										+ rect.height / 2)
						.setTextFont(p.getUiGothic(), rect.height * 0.8f)
						.setChecker(text -> {
							try {
								double value = Double.parseDouble(text);
								if (30 <= value && value < 600) {
									bpmSetter[0].accept(value);
								}
								return true;
							} catch (NumberFormatException e) {
								return false;
							}
						}).whenCanceled(s -> bpmSetter[0].accept(initialBpm[0]))
						.whenChanged(text -> p.getEditorManager().editTrack(
								tempoTrack, t -> bpmSetter[0]
										.accept(Double.parseDouble(text))))
						.whenClosed(() -> editingStake = Optional.empty())
						.showArea();
				editingStake = Optional.of(index);
				return true;
			}

			// If stake is clicked
			double beat = getBeatOnX(mouseX);
			Optional<TempoStake> mouseStake = tempoTrack.getStakeAtBeat(beat);
			if (mouseStake.isPresent()) {
				if (event.getButton() == CENTER) {
					p.getEditorManager().editTrack(tempoTrack,
							t -> t.removeBpmStake(beat));
				} else {
					clickingStake = mouseStake;
				}
			} else {
				clickingStake = p.getEditorManager().editTrackAndGet(tempoTrack,
						t -> t.addBpmStake(beat));
			}
			return true;
		}

		@Override
		public boolean mouseReleased(MouseEvent event, float mouseX,
				float mouseY) {
			if (super.mouseReleased(event, mouseX, mouseY)) return true;
			clickingStake = Optional.empty();
			if (stakeDragged) {
				stakeDragged = false;
			}
			return false;
		}

		private boolean stakeDragged = false;

		@Override
		public boolean mouseDragged(MouseEvent event, float mouseX,
				float mouseY) {
			if (!super.mouseDragged(event, mouseX, mouseY)) {
				clickingStake.ifPresent(mouseStake -> {
					double mouseSecond = trackContainer.getTimelineContainer()
							.getTimeOnPixelX(mouseX);
					tempoTrack.setStakeSecond(mouseStake, mouseSecond);
					stakeDragged = true;
				});
				return false;
			}
			return true;
		}

		private void beatLine(float y) {
			p.line(y, 0, y, height);
		}

	}

	private class Rectangle<T> {
		public final float x, y, width, height;
		public T t;

		private Rectangle(float x, float y, float width, float height, T t) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.t = t;
		}

		private boolean in(float ax, float ay) {
			return x <= ax && ax < x + width && y <= ay && ay < y + height;
		}
	}

}
