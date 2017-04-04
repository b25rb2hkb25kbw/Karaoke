package net.seiko_comb.onkohdondo.karaoke.editor.output;

public class OutputControlerStatus {
	protected OutputControler controler;
	protected long startMillis;
	protected double time;

	public OutputControlerStatus(OutputControler controler, long startMillis) {
		this.controler = controler;
		this.startMillis = startMillis;
		this.time = 0;
	}
	
	public double getTime() {
		return time;
	}
}
