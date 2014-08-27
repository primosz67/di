package com.tahona.bus;

import junit.framework.Assert;

import org.junit.Test;

public class EventBusTest {

	@Test
	public void test() {
		EventBus.subscribe(this);

		MyEvent event = new MyEvent();
		EventBus.inform(event);

		boolean condition = event.isExecuted().equals(1L);

		Assert.assertTrue(condition);

	}

	class Cs {

		@Subscribe
		private void executeEvent(MyEvent event) {
			event.assertTrue();
		}
	}

	class Te extends Cs {
		@Subscribe
		private void executeEvent(MyEvent event) {
			event.assertTrue();
		}

	}

	@Test
	public void testMulti() {
		EventBus.subscribe(this);
		EventBus.subscribe(new Te());

		MyEvent event = new MyEvent();
		EventBus.inform(event);

		Assert.assertTrue(event.isExecuted() == 3);
	}

	private static class MyEvent extends Event {

		private boolean executed;
		private long i = 0;

		public void assertTrue() {
			i++;
		}

		public Long isExecuted() {
			return this.i;
		}

	}

	@Subscribe
	private void executeEvent(MyEvent event) {
		event.assertTrue();
	}
}
