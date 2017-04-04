package net.seiko_comb.onkohdondo.karaoke.editor.data;

import java.time.Duration;

import net.seiko_comb.onkohdondo.karaoke.sound.FFT;
import net.seiko_comb.onkohdondo.util.MinMaxListDouble;

public class PowerGraphTrack extends Track {
	private MinMaxListDouble resultList;

	private boolean calculating;
	private FFT fft;

	private SoundTrack soundTrack;

	public PowerGraphTrack(SoundTrack soundTrack) {
		calculating = false;
		this.resultList = new MinMaxListDouble(
				new double[soundTrack.getSoundFile().getSampleCount()]);
		this.soundTrack = soundTrack;

		this.calculate();
	}

	static int cnt = 1;

	public boolean calculate() {
		if (calculating) return false;
		calculating = true;
		new Thread(this::internalCalculate).start();
		return true;
	}

	private void internalCalculate() {
		long startMillis = System.currentTimeMillis();
		double[] data = soundTrack.getSoundFile().getChannels().get(0)
				.getWaveform();
		fft = new FFT(18);
		int res = fft.SIZE % 2;

		// Hamming Window FFT
		double[] hammingRe = new double[fft.LEN];
		double[] hammingIm = new double[fft.LEN];
		double hammingDuration = 0.4;
		int hammingLength = (int) (soundTrack.getSoundFile().getSampleRate()
				* hammingDuration);
		for (int i = 0; i < Math.min(hammingLength, fft.LEN); i++) {
			fft.f[i] = 0.54
					- 0.46 * Math.cos(Math.PI + Math.PI * i / hammingLength);
		}
		for (int i = hammingLength; i < fft.LEN; i++) {
			fft.f[i] = 0;
		}
		// for (int i = 0; i < fft.LEN; i++) {
		// fft.f[i] = 0;
		// }
		// fft.f[0] = 1;
		fft.fft();
		System.arraycopy(fft.re[res], 0, hammingRe, 0, fft.LEN);
		System.arraycopy(fft.im[res], 0, hammingIm, 0, fft.LEN);
		// System.out.println(hammingLength + "\t" + fft.LEN);

		int fullLength = soundTrack.getSoundFile().getSampleCount();
		int chunkLength = fft.LEN - hammingLength;
		// int cnts = cnt - 1;
		// int cnte = cnt++;
		// int count = cnt++;
		// for (int k = 0; k < count; k++)
		for (int j = 0; j < fullLength; j += chunkLength) {
			for (int i = 0; i < chunkLength; i++) {
				if (j + i < fullLength) {
					fft.f[i] = Math.abs(data[j + i]);
				} else {
					fft.f[i] = 0;
				}
			}
			for (int i = chunkLength; i < fft.LEN; i++) {
				if (0 <= j + i - fft.LEN && j + i - fft.LEN < fullLength) {
					fft.f[i] = Math.abs(data[j + i - fft.LEN]);
				} else {
					fft.f[i] = 0;
				}
			}
			fft.fft();
			for (int i = 0; i < fft.LEN; i++) {
				double re = hammingRe[i] * fft.re[res][i]
						- hammingIm[i] * fft.im[res][i];
				double im = hammingRe[i] * fft.im[res][i]
						+ hammingIm[i] * fft.re[res][i];
				fft.re[res][i] = re;
				fft.im[res][i] = im;
			}
			fft.ifft();
			// System.out.println(k + "\t" + Arrays.hashCode(fft.f));
			for (int i = 0; i < chunkLength; i++) {
				if (j + i < fullLength) resultList.set(j + i, fft.f[i]);
			}
		}
		this.calculating = false;
		long endMillis = System.currentTimeMillis();
		Duration duration = Duration.ofMillis(endMillis - startMillis);
		System.out.println(duration);
	}

	public MinMaxListDouble getResultList() {
		return resultList;
	}

	public boolean isCalculating() {
		return calculating;
	}

	public float getListMin(double tLow, double tHigh) {
		double rate = soundTrack.getSoundFile().getSampleRate();
		return (float) resultList.min(tLow * rate, tHigh * rate);
	}

	public float getListMax(double tLow, double tHigh) {
		double rate = soundTrack.getSoundFile().getSampleRate();
		return (float) resultList.max(tLow * rate, tHigh * rate);
	}
}
