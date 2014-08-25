package com.tahona.bus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tahona.commons.MultiMap;
import com.tahona.commons.MultiMapFactory;
import com.tahona.di.ReflectionUtils;

public class EventBus {

	private final static EventBus INSTANCE = new EventBus();

	private EventBus() {
	}

	private final static HashMap<Class<? extends Event>, List<Object>> map = new HashMap<Class<? extends Event>, List<Object>>();
	private final static HashMap<Object, Map<Class<? extends Event>, Collection<Method>>> subscriberAndMethods = new HashMap<Object, Map<Class<? extends Event>, Collection<Method>>>();

	// private final HashMap<Object, List<Method>> subscriberAndMethods
	// = new HashMap<Object, List<Method>>();

	public static void subscribe(final Object subscriber) {
		final List<Method> methods = ReflectionUtils.getMethods(subscriber, Subscribe.class);
		final MultiMap<Class<? extends Event>, Method> meth = MultiMapFactory.create();

		for (final Method method : methods) {
			final Class<?>[] parameterTypes = method.getParameterTypes();
			final Class<? extends Event> eventClass = (Class<? extends Event>) parameterTypes[0];

			meth.put(eventClass, method);
		}

		final Map<Class<? extends Event>, Collection<Method>> asMap = meth.asMap();
		subscriberAndMethods.put(subscriber, asMap);

		//
		// for (Object subscriber : subscriberAndMethods.keySet()) {
		// List<Method> list = subscriberAndMethods.get(subscriber);
		// for (Method method : list) {
		// if (hasMethodProperEvent(method, event)) {
		//
		// }
		// }
		// }

		// TODO
	}

	public void subscribe(final Class<? extends Event> class1, final Object subscriber) {
		if (false == map.containsKey(class1)) {
			map.put(class1, new ArrayList<Object>());
		}

		final List<Object> list = map.get(class1);
		if (notContainSubscriber(list, subscriber)) {
			list.add(subscriber);
		}
	}

	private boolean notContainSubscriber(final List<? extends Object> list, final Object subscriber) {
		for (final Object Object : list) {
			if (Object.equals(subscriber)) {
				return false;
			}
		}
		return true;
	}

	public static void inform(final Event event) {
		final boolean invoked = false;
		final Set<Object> keySet = subscriberAndMethods.keySet();

		if (isRegisteredForMethods(event)) {
			for (final Object object : keySet) {
				final Map<Class<? extends Event>, Collection<Method>> map2 = subscriberAndMethods.get(object);
				final Collection<Method> methodsToInvoke = map2.get(event.getClass());

				if (methodsToInvoke != null && false == methodsToInvoke.isEmpty()) {
					for (final Method method : methodsToInvoke) {
						ReflectionUtils.invokeMethodWith(object, method, event);
					}
				}
			}
		}

		if (map.containsKey(event.getClass())) {
			// TODO method execution witn @EventAction

			if (false == invoked) {
				for (final Object subscriber : map.get(event.getClass())) {
					if (subscriber instanceof EventSubscriber) {
						((EventBus) subscriber).inform(event);
					}
				}
			}
		}
	}

	private static boolean isRegisteredForMethods(final Event event) {
		final Collection<Map<Class<? extends Event>, Collection<Method>>> values = subscriberAndMethods.values();
		for (final Map<Class<? extends Event>, Collection<Method>> map : values) {
			if (map.containsKey(event.getClass())) {
				return true;
			}
		}

		return false;
	}

	public static EventBus getInstance() {
		return INSTANCE;
	}

	public static void clear() {
		map.clear();
		subscriberAndMethods.clear();
	}

}
