package com.tahona.bus;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class EventBusTest {

	@Before
	public void setup() {
		EventBus.clear();
	}

	@Test
	public void test() {
		EventBus.subscribe(this);

		final MyEvent event = new MyEvent();
		EventBus.inform(event);

		Assert.assertEquals(event.getExecutedCount(), 1L);
	}

	class Cs {

		@Subscribe
		private void executeEvent(final MyEvent event) {
			event.assertTrue();
		}
	}

	class Te extends Cs {
		@Subscribe
		private void executeEvent(final MyEvent event) {
			event.assertTrue();
		}
	}

	@Test
	public void testUnsubscribe() {
		Te subscriber = new Te();
		EventBus.subscribe(subscriber);
		EventBus.unsubscribe(subscriber);
		
		final MyEvent event = new MyEvent();
		EventBus.inform(event);

		Assert.assertEquals(0, event.getExecutedCount());
	}
	
	@Test
	public void testMulti() {
		EventBus.subscribe(this);
		EventBus.subscribe(new Te());

		final MyEvent event = new MyEvent();
		EventBus.inform(event);

		Assert.assertEquals(event.getExecutedCount(), 3L);
	}

	private static class MyEvent extends Event {
		private long i = 0;

		public void assertTrue() {
			i++;
		}

		public long getExecutedCount() {
			return this.i;
		}

	}

	@Subscribe
	private void executeEvent(final MyEvent event) {
		event.assertTrue();
	}
}
