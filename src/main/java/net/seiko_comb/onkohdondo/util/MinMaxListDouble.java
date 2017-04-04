package net.seiko_comb.onkohdondo.util;

public class MinMaxListDouble {
	private RMQDouble minRMQ, maxRMQ;
	private final double[] data;
	private double defaultValue;

	public MinMaxListDouble(int size) {
		this(new double[size]);
	}

	public MinMaxListDouble(double[] data) {
		this.data = data;
		init();
	}

	public void init() {
		minRMQ = new RMQDouble(data.length / 100, false,
				Double.POSITIVE_INFINITY);
		maxRMQ = new RMQDouble(data.length / 100, true,
				Double.NEGATIVE_INFINITY);
		for (int i = 0; i < data.length / 100; i++) {
			double min = Double.POSITIVE_INFINITY,
					max = Double.NEGATIVE_INFINITY;
			for (int j = i * 100; j < Math.min((i + 1) * 100,
					data.length); j++) {
				min = Math.min(min, data[j]);
				max = Math.max(max, data[j]);
			}
			minRMQ.set(i, min);
			maxRMQ.set(i, max);
		}
	}

	public void set(int index, double value) {
		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		data[index] = value;
		int section = index / 100;
		for (int i = section * 100; i < Math.min((section + 1) * 100,
				data.length); i++) {
			min = Math.min(min, data[i]);
			max = Math.max(max, data[i]);
		}
		minRMQ.set(section, min);
		maxRMQ.set(section, max);
	}

	public double min(double from, double to) {
		return getFromRMQ(minRMQ, from, to);
	}

	public double max(double from, double to) {
		return getFromRMQ(maxRMQ, from, to);
	}

	public double getFromRMQ(RMQDouble rmq, double from, double to) {
		int start = (int) from + 1;
		int end = (int) to + 1;
		if (start < 0) {
			start = 0;
		}
		if (end > data.length) {
			end = data.length;
		}

		int startSec = start / 100 + 1;
		int endSec = end / 100 + 1;

		double ret = rmq.getInit();
		if (endSec - startSec <= 1) {
			for (int i = start; i < end; i++) {
				ret = rmq.min(ret, data[i]);
			}
		} else {
			for (int i = start; i < startSec * 100; i++) {
				ret = rmq.min(ret, data[i]);
			}
			ret = rmq.min(ret, rmq.get(startSec, endSec));
			for (int i = endSec * 100; i < end; i++) {
				ret = rmq.min(ret, data[i]);
			}
		}
		ret = rmq.min(ret, getDataAt(from));
		ret = rmq.min(ret, getDataAt(to));
		return ret;
	}

	public double getDataAt(double d) {
		int index = (int) d;
		double t = d - index;
		int index1 = index + 1;
		return (1 - t)
				* (index < 0 ? 0 : index >= data.length ? 0 : data[index])
				+ t * (index1 < 0 ? 0
						: index1 >= data.length ? 0 : data[index1]);
	}

	public double overallMin() {
		return min(-1, data.length + 1);
	}

	public double overallMax() {
		return max(-1, data.length + 1);
	}

	public double getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(double defaultValue) {
		this.defaultValue = defaultValue;
	}
}
