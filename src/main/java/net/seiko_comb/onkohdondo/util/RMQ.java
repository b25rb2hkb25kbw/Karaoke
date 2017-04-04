package net.seiko_comb.onkohdondo.util;

import java.util.Arrays;
import java.util.Comparator;

public class RMQ<T> {
	private int size;
	private Object[] dat;
	private Comparator<T> comp;
	private T init;

	public RMQ(int n, Comparator<T> comp, T init) {
		size = 1;
		while (size < n)
			size *= 2;
		dat = new Object[2 * size - 1];
		Arrays.fill(dat, init);
		this.comp = comp;
		this.init = init;
	}

	public void set(int k, T a) {
		k += size - 1;
		dat[k] = a;
		while (k > 0) {
			k = (k - 1) / 2;
			dat[k] = min(dat[k * 2 + 1], dat[k * 2 + 2]);
		}
	}

	@SuppressWarnings("unchecked")
	public T get(int a, int b, int k, int l, int r) {
		if (r <= a || b <= l) return init;
		if (a <= l && r <= b)
			return (T) dat[k];
		else {
			T vl = get(a, b, k * 2 + 1, l, (l + r) / 2);
			T vr = get(a, b, k * 2 + 2, (l + r) / 2, r);
			return (T) min(vl, vr);
		}
	}

	public T get(int a, int b) {
		return get(a, b, 0, 0, size);
	}

	@SuppressWarnings("unchecked")
	private Object min(Object t, Object u) {
		if (comp.compare((T) t, (T) u) > 0) {
			return u;
		} else {
			return t;
		}
	}

}
