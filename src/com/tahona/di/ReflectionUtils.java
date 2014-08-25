package com.tahona.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtils {

	public static void invokeMethodWith(final Object bean, final Class<? extends Annotation> class1,
			final Object... objects) {
		final Method[] methods = bean.getClass().getDeclaredMethods();
		for (final Method method : methods) {
			method.setAccessible(true);
			if (method.isAnnotationPresent(class1)) {
				try {
					method.invoke(bean, objects);
				} catch (final IllegalArgumentException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static List<Method> getMethods(final Object bean, final Class class1) {
		final Method[] methods = bean.getClass().getDeclaredMethods();

		final List<Method> methodsList = new ArrayList<Method>();

		for (final Method method : methods) {
			method.setAccessible(true);
			if (method.isAnnotationPresent(class1)) {
				methodsList.add(method);
			}
		}
		return methodsList;
	}

	public static void invokeMethodWith(Object bean, Method method, Object... objects) {
		method.setAccessible(true);
		try {
			method.invoke(bean, objects);
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
