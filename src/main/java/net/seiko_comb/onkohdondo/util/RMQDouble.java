package net.seiko_comb.onkohdondo.util;

import java.util.Arrays;

public class RMQDouble {
	private int size;
	private double[] dat;
	private double init;
	private boolean max;

	public RMQDouble(int n, boolean max, double init) {
		size = 1;
		while (size < n)
			size *= 2;
		dat = new double[2 * size - 1];
		Arrays.fill(dat, init);
		this.max = max;
		this.init = init;
	}

	public void set(int k, double a) {
		// if (max) System.out.println(k + "\t" + a);
		k += size - 1;
		dat[k] = a;
		// System.out.print(k + " -> ");
		while (k > 0) {
			k = (k - 1) / 2;
			dat[k] = min(dat[k * 2 + 1], dat[k * 2 + 2]);
			// System.out.print(k + "=" + dat[k] + " ");
		}
		// System.out.println();
	}

	public double get(int a, int b, int k, int l, int r) {
		double ret;
		if (r <= a || b <= l) {
			ret = init;
		} else if (a <= l && r <= b) {
			ret = dat[k];
		} else {
			double vl = get(a, b, k * 2 + 1, l, (l + r) / 2);
			double vr = get(a, b, k * 2 + 2, (l + r) / 2, r);
			ret = min(vl, vr);
		}
		return ret;
	}

	public double get(int a, int b) {
		return get(a, b, 0, 0, size);
	}

	public double min(double a, double b) {
		if (max)
			return Math.max(a, b);
		else return Math.min(a, b);
	}

	public double overallMin() {
		System.out.println("size = " + size);
		return get(0, size, 0, 0, size);
	}

	public double getInit() {
		return init;
	}
}
