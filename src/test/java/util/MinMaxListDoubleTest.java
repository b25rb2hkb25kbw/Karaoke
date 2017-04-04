package util;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.seiko_comb.onkohdondo.util.MinMaxListDouble;

public class MinMaxListDoubleTest {

	@Test
	public void test() {
		MinMaxListDouble list = new MinMaxListDouble(8);
		list.set(0, 3);
		list.set(1, 1);
		list.set(2, 4);
		list.set(3, 1);
		list.set(4, 5);
		list.set(5, 9);
		list.set(6, 2);
		list.set(7, 6);
		assertThat(list.min(0, 4), is(closeTo(1, 1E-8)));
		assertThat(list.min(0, 3), is(closeTo(1, 1E-8)));
		assertThat(list.min(0, 2), is(closeTo(1, 1E-8)));
		assertThat(list.min(0, 1), is(closeTo(1, 1E-8)));
		assertThat(list.min(4, 8), is(closeTo(0, 1E-8)));
		assertThat(list.min(4, 5), is(closeTo(5, 1E-8)));
		assertThat(list.min(4, 6), is(closeTo(2, 1E-8)));
		assertThat(list.min(1.5, 2), is(closeTo(2.5, 1E-8)));
		assertThat(list.min(0, 7), is(closeTo(1, 1E-8)));
		assertThat(list.overallMin(), is(closeTo(0, 1E-8)));
		assertThat(list.overallMax(), is(closeTo(9, 1E-8)));
	}

}
