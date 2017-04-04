package util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.seiko_comb.onkohdondo.util.RMQDouble;

public class RMQDoubleTest {

	@Test
	public void test() {
		RMQDouble rmq = new RMQDouble(10, false, 300.0);
		rmq.set(0, 3.0);
		rmq.set(1, 1.0);
		rmq.set(2, 4.0);
		rmq.set(3, 1.0);
		rmq.set(4, 5.0);
		rmq.set(5, 9.0);
		rmq.set(6, 2.0);
		rmq.set(7, 6.0);
		assertThat(rmq.get(0, 4), is(1.0));
		assertThat(rmq.get(0, 3), is(1.0));
		assertThat(rmq.get(0, 2), is(1.0));
		assertThat(rmq.get(0, 1), is(3.0));
		assertThat(rmq.get(4, 8), is(2.0));
		assertThat(rmq.get(4, 5), is(5.0));
		assertThat(rmq.get(4, 6), is(5.0));
		assertThat(rmq.overallMin(), is(1.0));
	}

}
