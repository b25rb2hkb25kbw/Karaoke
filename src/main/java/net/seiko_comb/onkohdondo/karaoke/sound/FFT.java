package net.seiko_comb.onkohdondo.karaoke.sound;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;

public class FFT {
	private static final Scanner in = new Scanner(System.in);
	public final int SIZE, LEN;
	public final int[] move;
	public final double[] f;
	public final double[][] re, im;
	public final double[] wre, wim;

	private FFT nextFFT;

	public FFT(int size) {
		this(size, true);
	}

	private FFT(int size, boolean next) {
		this.SIZE = size;
		this.LEN = 1 << SIZE;

		f = new double[LEN];
		re = new double[2][LEN];
		im = new double[2][LEN];
		wre = new double[LEN];
		wim = new double[LEN];
		move = new int[LEN];
		nsdf = new double[LEN];

		for (int i = 0; i < LEN; i++) {
			wre[i] = cos(-2 * PI * i / LEN);
			wim[i] = sin(-2 * PI * i / LEN);

			int k = 0;
			for (int j = 0; j < SIZE; j++) {
				k = k * 2 + (i >> j) % 2;
			}
			move[i] = k;
		}

		if (next) nextFFT = new FFT(size + 1, false);
	}

	public void readDataFromStd() {
		for (int i = 0; i < LEN; i++)
			f[i] = in.nextDouble();
	}

	public void fft() {
		for (int i = 0; i < LEN; i++) {
			re[0][move[i]] = f[i];
			// I found it!!!!
			im[0][move[i]] = 0;
		}

		butterfly();
	}

	private void butterfly() {
		for (int i = 0; i < SIZE; i++) {
			int unit = 1 << (i + 1);
			int old = i % 2, cur = 1 - old;
			for (int j = 0; j < LEN / unit; j++) {
				int start = j * unit;
				for (int k = 0; k < unit; k++) {
					int p = k % (unit / 2) + start, q = p + unit / 2;
					int r = (int) (((long) k) * LEN / unit);
					re[cur][start + k] = re[old][p] + re[old][q] * wre[r]
							- im[old][q] * wim[r];
					im[cur][start + k] = im[old][p] + re[old][q] * wim[r]
							+ im[old][q] * wre[r];
				}
			}
		}
	}

	public void printResult() {
		System.out.println("result");
		for (int i = 0; i < LEN; i++) {
			System.out.format("%d %+1.8f %+1.8f\n", i,
					complexAbs(re[SIZE % 2][i], im[SIZE % 2][i]),
					complexArg(re[SIZE % 2][i], im[SIZE % 2][i]));
		}
		System.out.println();
	}

	public void ifft() {
		for (int i = 0; i < LEN; i++) {
			re[1][i] = re[SIZE % 2][i];
			im[1][i] = -im[SIZE % 2][i];
		}
		for (int i = 0; i < LEN; i++) {
			re[0][i] = re[1][move[i]];
			im[0][i] = im[1][move[i]];
		}
		butterfly();
		for (int i = 0; i < LEN; i++) {
			f[i] = re[SIZE % 2][i] / LEN;
		}
		// double[] g = new double[LEN];
		// for (int i = 0; i < LEN; i++) {
		// double a = complexAbs(re[SIZE % 2][i], im[SIZE % 2][i]) / LEN;
		// double b = complexArg(re[SIZE % 2][i], im[SIZE % 2][i]);
		// for (int j = 0; j < LEN; j++) {
		// g[j] += a * cos(2 * PI * i * j / LEN + b);
		// }
		// }
	}

	public static void dumpComplex(double re, double im) {
		System.out.format("%+1.8f%+1.8fi\n", re, im);
	}

	public static double complexAbs(double re, double im) {
		return sqrt(re * re + im * im);
	}

	public static double squareSum(double re, double im) {
		return re * re + im * im;
	}

	public static double complexArg(double re, double im) {
		return atan2(im, re);
	}

	public static void main(String[] args)
			throws LineUnavailableException, IOException {
	}

	public final double[] nsdf;
	public double frequency, clearity, volume;

	public void analyze() {
		nextFFT.autoCorrelation(f);
		double m0 = 0;
		for (int i = 0; i < LEN; i++)
			m0 += f[i] * f[i];
		m0 *= 2;
		for (int i = 0; i < LEN; i++) {
			nsdf[i] = 2 * nextFFT.f[i] / m0;
			m0 -= f[i] * f[i];
			m0 -= f[LEN - i - 1] * f[LEN - i - 1];
		}
		double k = 0.9;
		boolean search = false;
		double sectionMax = 0;
		int sectionMaxX = 0;
		double overallMax = 0;
		int overallMaxX = 0;
		for (int i = 1; i < LEN - 1; i++) {
			if (nsdf[i] < 0 && 0 <= nsdf[i + 1]) {
				search = true;
			} else if (nsdf[i] >= 0 && 0 > nsdf[i + 1]) {
				if (overallMax / k < sectionMax) {
					overallMax = sectionMax;
					overallMaxX = sectionMaxX;
				}
				search = false;
			}
			if (search && 0 <= nsdf[i - 1] && nsdf[i - 1] < nsdf[i]
					&& nsdf[i] > nsdf[i + 1]) {
				if (sectionMax < nsdf[i]) {
					sectionMax = nsdf[i];
					sectionMaxX = i;
				}
			}
		}
		if (overallMaxX != 0) {
			int i = overallMaxX;
			double a = (nsdf[i + 1] + nsdf[i - 1]) / 2 - nsdf[i];
			double b = (nsdf[i + 1] - nsdf[i - 1]) / 2;
			double c = nsdf[i];
			double center = -b / (2 * a) + i;
			frequency = 44100 / center;
			clearity = (4 * a * c - b * b) / (4 * a);
		}
		volume = 0;
		for (int i = 0; i < LEN; i++) {
			volume = Math.max(volume, Math.abs(f[i]));
		}
	}

	// public void analyze2() {
	// for (int i = 0; i < LEN; i++) {
	// double r = 0, d = 0, m = 0, n = 0;
	// for (int j = 0; j < LEN - i; j++) {
	// r += f[j] * f[j + i];
	// d += (f[j] - f[j + i]) * (f[j] - f[j + i]);
	// m += f[j] * f[j] + f[j + i] * f[j + i];
	// }
	// n = 2 * r / m;
	// System.out.format("%d %+3.8f%n", i, n);
	// }
	// System.out.println();
	// }

	private void autoCorrelation(double[] g) {
		for (int i = 0; i < LEN / 2; i++) {
			f[i] = g[i];
		}
		for (int i = LEN / 2; i < LEN; i++) {
			f[i] = 0;
		}
		fft();
		for (int i = 0; i < LEN; i++) {
			re[SIZE % 2][i] = re[SIZE % 2][i] * re[SIZE % 2][i]
					+ im[SIZE % 2][i] * im[SIZE % 2][i];
			im[SIZE % 2][i] = 0;
		}
		ifft();
	}

	public void clear() {
		for (int i = 0; i < LEN; i++) {
			f[i] = 0;
			for (int j = 0; j < 2; j++) {
				re[j][i] = 0;
				im[j][i] = 0;
			}
			nsdf[i] = 0;
		}
		if (nextFFT != null) nextFFT.clear();
	}
}
