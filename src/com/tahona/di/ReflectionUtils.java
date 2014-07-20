package com.tahona.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtils {

	public static void invokeMethodWith(Object bean, Class<? extends Annotation> class1, Object... objects ) {
		final Method[] methods = bean.getClass().getDeclaredMethods();
		for (final Method method : methods) {
			method.setAccessible(true);
			if (method.isAnnotationPresent(class1)) {
				try {
					method.invoke(bean, objects );
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

}
