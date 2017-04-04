package net.seiko_comb.onkohdondo.karaoke.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.seiko_comb.onkohdondo.karaoke.editor.data.Track;

public class TempoTrack extends Track {

	private double defaultBpm = 0, afterBpm = 120;
	private List<TempoStake> stakes = new ArrayList<>();
	private List<MeasureStake> measureStakes = new ArrayList<>();

	public TempoTrack() {
		stakes.add(new TempoStake(this, 0, 0.55));
		measureStakes.add(new MeasureStake(this, 0, 4, 4));
	}

	public double getSecond(double beat) {
		TempoStake stake = null;
		double bpm = 0;
		if (beat < stakes.get(0).getBeat()) {
			stake = stakes.get(0);
			if (defaultTempoIsSet()) {
				bpm = defaultBpm;
			} else {
				if (stakes.size() == 1) bpm = afterBpm;
			}
		} else if (stakes.get(stakes.size() - 1).getBeat() <= beat) {
			stake = stakes.get(stakes.size() - 1);
			if (afterTempoIsSet()) {
				bpm = afterBpm;
			}
		}
		if (bpm != 0) {
			return stake.getSecond()
					+ (beat - stake.getBeat()) / 480 * (60 / bpm);
		}
		for (int i = 1; i < stakes.size(); i++) {
			TempoStake before = stakes.get(i - 1), after = stakes.get(i);
			if (i + 1 == stakes.size() || beat < after.getBeat()) {
				return map(beat, before.getBeat(), after.getBeat(),
						before.getSecond(), after.getSecond());
			}
		}
		throw new IllegalStateException("Unexpected : internal error");
	}

	public double getBeat(double second) {
		TempoStake stake = null;
		double bpm = 0;
		if (second < stakes.get(0).getSecond()) {
			stake = stakes.get(0);
			if (defaultTempoIsSet()) {
				bpm = defaultBpm;
			} else {
				if (stakes.size() == 1) bpm = afterBpm;
			}
		} else if (stakes.get(stakes.size() - 1).getSecond() <= second) {
			stake = stakes.get(stakes.size() - 1);
			if (afterTempoIsSet()) {
				bpm = afterBpm;
			}
		}
		if (bpm != 0) {
			return stake.getBeat()
					+ (second - stake.getSecond()) / (60 / bpm) * 480;
		}
		for (int i = 1; i < stakes.size(); i++) {
			TempoStake before = stakes.get(i - 1), after = stakes.get(i);
			if (i + 1 == stakes.size() || second < after.getSecond()) {
				return map(second, before.getSecond(), after.getSecond(),
						before.getBeat(), after.getBeat());
			}
		}
		throw new IllegalStateException("Unexpected : internal error");
	}

	public double getBpmAt(int index) {
		if (index < 0) {
			return getDefaultBpm();
		} else if (index + 1 >= stakes.size()) {
			return getAfterBpm();
		} else {
			TempoStake before = stakes.get(index),
					after = stakes.get(index + 1);
			return (after.getBeat() - before.getBeat()) / 480 * 60
					/ (after.getSecond() - before.getSecond());
		}
	}

	public double getDefaultBpm() {
		if (defaultTempoIsSet()) {
			return defaultBpm;
		} else {
			return getBpmAt(0);
		}
	}

	public void setDefaultBpm(double defaultBpm) {
		if (defaultBpm <= 0)
			removeBeforeTempo();
		else this.defaultBpm = defaultBpm;
	}

	public double getAfterBpm() {
		if (afterTempoIsSet()) {
			return afterBpm;
		} else {
			return getBpmAt(stakes.size() - 2);
		}
	}

	public void setAfterBpm(double afterBpm) {
		if (afterBpm <= 0)
			removeAfterTempo();
		else this.afterBpm = afterBpm;
	}

	public boolean defaultTempoIsSet() {
		return defaultBpm != 0;
	}

	public boolean afterTempoIsSet() {
		return afterBpm != 0;
	}

	public List<TempoStake> getStakes() {
		return stakes;
	}

	static public final double map(double value, double start1, double stop1,
			double start2, double stop2) {
		return start2
				+ (stop2 - start2) * ((value - start1) / (stop1 - start1));
	}

	public Optional<TempoStake> addBpmStake(double beat) {
		double second = getSecond(beat);
		if (beat < 0) {
			stakes.forEach(s -> s.setBeat(b -> b - beat));
			measureStakes.forEach(s -> s.setBeat(b -> b - beat));
			TempoStake ret = new TempoStake(this, 0, second);
			stakes.add(0, ret);
			defaultBpm = 0;
			return Optional.of(ret);
		} else {
			for (int i = stakes.size() - 1; i >= 0; i--) {
				TempoStake stake = stakes.get(i);
				if (Math.abs(stake.getBeat() - beat) < 1E-3) {
					return Optional.empty();
				} else if (stake.getBeat() < beat) {
					if (i + 1 == stakes.size()) {
						afterBpm = 0;
					}
					TempoStake ret = new TempoStake(this, beat, second);
					stakes.add(i + 1, ret);
					return Optional.of(ret);
				}
			}
		}
		return Optional.empty();
	}

	public void removeBpmStake(double beat) {
		if (stakes.size() == 1) return;
		for (int i = 0; i < stakes.size(); i++) {
			TempoStake stake = stakes.get(i);
			if (Math.abs(stake.getBeat() - beat) < 1E-3) {
				removeStakeOnIndex(i);
				return;
			}
		}
	}

	public void removeStakeOnIndex(int i) {
		if (i == 0) {
			defaultBpm = getBpmAt(0);
			if (stakes.size() == 2 && afterBpm == 0) {
				afterBpm = getBpmAt(0);
			}
			stakes.remove(0);
			double diff = stakes.get(0).getBeat();
			stakes.forEach(s -> s.setBeat(b -> b - diff));
			measureStakes.forEach(s -> s.setBeat(b -> b - diff));
		} else if (i == stakes.size() - 1) {
			afterBpm = getBpmAt(stakes.size() - 2);
			stakes.remove(i);
		} else {
			stakes.remove(i);
		}
	}

	public void removeBeforeTempo() {
		defaultBpm = 0;
	}

	public void removeAfterTempo() {
		if (stakes.size() > 1) {
			afterBpm = 0;
		}
	}

	public void setStakeSecond(TempoStake stake, double second) {
		int index = stakes.indexOf(stake);
		if (index == -1) return;
		if (0 <= index - 1 && second < stakes.get(index - 1).getSecond()) {
			return;
		}
		if (index + 1 < stakes.size()
				&& stakes.get(index + 1).getSecond() < second) {
			return;
		}
		stake.setSecond(a -> second);
	}

	public Optional<TempoStake> getStakeAtBeat(double beat) {
		return stakes.stream().filter(s -> Math.abs(s.getBeat() - beat) < 1E-3)
				.findFirst();
	}

	public Optional<MeasureStake> getMeasureStakeAtBeat(double beat) {
		return measureStakes.stream()
				.filter(s -> Math.abs(s.getBeat() - beat) < 1E-3).findFirst();
	}

	public List<MeasureStake> getMeasureStakes() {
		return measureStakes;
	}

	public MeasureStake getMeasureStakeAt(int index) {
		if (index < 0) index = 0;
		if (index >= measureStakes.size()) index = measureStakes.size() - 1;
		return measureStakes.get(index);
	}

	public void removeMeasureStake(double beat) {
		if (measureStakes.size() == 1) return;
		for (int i = 0; i < measureStakes.size(); i++) {
			MeasureStake stake = measureStakes.get(i);
			if (Math.abs(stake.getBeat() - beat) < 1E-3) {
				removeMeasureStakeonIndex(i);
			}
		}
	}

	public void removeMeasureStakeonIndex(int i) {
		measureStakes.remove(i);
	}

	public int getMeaasureStakeIndexBeforeBeat(double beat) {
		for (int i = measureStakes.size() - 1; i >= 0; i--) {
			MeasureStake stake = measureStakes.get(i);
			if (stake.getBeat() <= beat) {
				return i;
			}
		}
		return 0;
	}

	public void addMeasureStake(double beat) {
		int index = getMeaasureStakeIndexBeforeBeat(beat);
		if (Math.abs(beat - measureStakes.get(index).getBeat()) < 1E-3) return;
		if (index + 1 < measureStakes.size() && Math
				.abs(beat - measureStakes.get(index + 1).getBeat()) < 1E-3)
			return;
		MeasureStake stake = measureStakes.get(index);
		MeasureStake newStake = new MeasureStake(this, beat,
				stake.getBeatCount(), stake.getBaseCount());
		if (beat < stake.getBeat()) {
			measureStakes.add(index, newStake);
		}else{
			measureStakes.add(index+1, newStake);
		}
	}
}
