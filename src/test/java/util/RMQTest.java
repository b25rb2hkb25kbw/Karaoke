package util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Comparator;

import org.junit.Test;

import net.seiko_comb.onkohdondo.util.RMQ;

public class RMQTest {

	@Test
	public void test() {
		RMQ<Integer> rmq = new RMQ<Integer>(10, Integer::compareTo,
				Integer.MAX_VALUE);
		rmq.set(0, 3);
		rmq.set(1, 1);
		rmq.set(2, 4);
		rmq.set(3, 1);
		rmq.set(4, 5);
		rmq.set(5, 9);
		rmq.set(6, 2);
		rmq.set(7, 6);
		assertThat(rmq.get(0, 4), is(1));
		assertThat(rmq.get(0, 3), is(1));
		assertThat(rmq.get(0, 2), is(1));
		assertThat(rmq.get(0, 1), is(3));
		assertThat(rmq.get(4, 8), is(2));
		assertThat(rmq.get(4, 5), is(5));
		assertThat(rmq.get(4, 6), is(5));
	}

	@Test
	public void test2() {
		RMQ<Double> rmq = new RMQ<>(10, Comparator.naturalOrder(), 300.0);
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
	}

	@Test
	public void test3() {
		RMQ<Double> rmq = new RMQ<>(10, Comparator.reverseOrder(), -300.0);
		rmq.set(0, 3.0);
		rmq.set(1, 1.0);
		rmq.set(2, 4.0);
		rmq.set(3, 1.0);
		rmq.set(4, 5.0);
		rmq.set(5, 9.0);
		rmq.set(6, 2.0);
		rmq.set(7, 6.0);
		assertThat(rmq.get(0, 4), is(4.0));
		assertThat(rmq.get(0, 3), is(4.0));
		assertThat(rmq.get(0, 2), is(3.0));
		assertThat(rmq.get(0, 1), is(3.0));
		assertThat(rmq.get(4, 8), is(9.0));
		assertThat(rmq.get(4, 5), is(5.0));
		assertThat(rmq.get(4, 6), is(9.0));
	}
}
