package com.tahona.bus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tahona.commons.MultiMap;
import com.tahona.commons.MultiMapFactory;
import com.tahona.di.ReflectionUtils;

public class EventBus {

	private final static EventBus INSTANCE = new EventBus();

	private EventBus() {
	}

	private final Map<Class<? extends Event>, List<Object>> map = new ConcurrentHashMap<Class<? extends Event>, List<Object>>();
	private final Map<Object, Map<Class<? extends Event>, Collection<Method>>> subscriberAndMethods = new ConcurrentHashMap<Object, Map<Class<? extends Event>, Collection<Method>>>();

	// private final HashMap<Object, List<Method>> subscriberAndMethods
	// = new HashMap<Object, List<Method>>();

	public static void subscribe(final Object subscriber) {
		final EventBus bus = getInstance();
		final List<Method> methods = ReflectionUtils.getMethods(subscriber, Subscribe.class, true);
		final MultiMap<Class<? extends Event>, Method> meth = MultiMapFactory.create();

		for (final Method method : methods) {
			final Class<?>[] parameterTypes = method.getParameterTypes();
			final Class<? extends Event> eventClass = (Class<? extends Event>) parameterTypes[0];

			meth.put(eventClass, method);
		}

		final Map<Class<? extends Event>, Collection<Method>> asMap = meth.asMap();
		bus.subscriberAndMethods.put(subscriber, asMap);

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
	
	public static void unsubscribe(final Object subscriber) {
		getInstance().subscriberAndMethods.remove(subscriber);
		
		Map<Class<? extends Event>, List<Object>> eventsMap = getInstance().map;
		for (Class<? extends Event> eventClass : eventsMap.keySet()) {
			List<Object> list = eventsMap.get(eventClass);
			list.remove(subscriber);
		}
	}
	

	@Deprecated
	public static void subscribe(final Class<? extends Event> class1, final Object subscriber) {
		final EventBus inst = getInstance();
		
		if (false == inst.map.containsKey(class1)) {
			inst.map.put(class1, new ArrayList<Object>());
		}

		final List<Object> list = inst.map.get(class1);
		if (notContainSubscriber(list, subscriber)) {
			list.add(subscriber);
		}
	}

	private static boolean notContainSubscriber(final List<? extends Object> list, final Object subscriber) {
		for (final Object Object : list) {
			if (Object.equals(subscriber)) {
				return false;
			}
		}
		return true;
	}

	public static synchronized void inform(final Event event) {
		final EventBus bus = getInstance();
		boolean invoked = false;
		final Set<Object> keySet = bus.subscriberAndMethods.keySet();

		if (isRegisteredForMethods(event)) {
			for (final Object object : keySet) {
				final Map<Class<? extends Event>, Collection<Method>> map2 = bus.subscriberAndMethods.get(object);

				if (map2 != null) {
					final Collection<Method> methodsToInvoke = map2.get(event.getClass());

					if (methodsToInvoke != null && false == methodsToInvoke.isEmpty()) {
						for (final Method method : methodsToInvoke) {
							ReflectionUtils.invokeMethodWith(object, method, event);
							invoked = true;
						}
					}
				}
			}
		}
//		if (false == invoked) {
			if (bus.map.containsKey(event.getClass())) {
				// TODO method execution witn @EventAction
			
				for (final Object subscriber : bus.map.get(event.getClass())) {
					final boolean isInstanceOf = subscriber instanceof EventSubscriber;
					if (isInstanceOf) {
						((EventSubscriber) subscriber).inform(event);
					}
				}
			}
//		}
	}

	private static boolean isRegisteredForMethods(final Event event) {
		final Collection<Map<Class<? extends Event>, Collection<Method>>> values = getInstance().subscriberAndMethods
				.values();
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
		final EventBus bus = getInstance();
		bus.map.clear();
		bus.subscriberAndMethods.clear();
	}

}
