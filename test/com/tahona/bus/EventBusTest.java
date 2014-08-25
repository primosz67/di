package com.tahona.bus;

import junit.framework.Assert;

import org.junit.Test;

import com.tahona.bus.Event;
import com.tahona.bus.EventBus;
import com.tahona.bus.Subscribe;

public class EventBusTest {

	@Test
	public void test() {
		EventBus.subscribe(this);

		MyEvent event = new MyEvent();
		EventBus.inform(event);

		Assert.assertTrue(event.isExecuted());

	}

	private static class MyEvent extends Event {

		private boolean executed;

		public void assertTrue(boolean b) {
			this.executed = b;
		}

		public boolean isExecuted() {
			return executed;
		}

	}

	@Subscribe
	private void executeEvent(MyEvent event) {
		event.assertTrue(true);
	}

}
