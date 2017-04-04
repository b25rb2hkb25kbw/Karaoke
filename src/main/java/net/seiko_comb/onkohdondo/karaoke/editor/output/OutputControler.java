package net.seiko_comb.onkohdondo.karaoke.editor.output;

public interface OutputControler {
	/**
	 * Get the wave value, between -1.0(inclusive) and 1.0(exclusive).
	 * 
	 * For current version, this is only for two channels.
	 * 
	 * @param channel
	 *            the channel index. Since the number of channels is limited
	 *            only to two, this value must be 0 or 1.
	 * @param time
	 *            The elapsed time since the play-back started. This value must
	 *            be positive number. Also, this method must be called in
	 *            non-decreasing order for each channel.
	 * @return The wave value, between -1.0 and 1.0.
	 */
	public double getOutput(int channel, double time);

	public default boolean end() {
		return false;
	}
}
