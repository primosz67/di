package com.tahona.utils.di;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tahona.utils.di.annotation.Wire;

public class Injector {

	private BeanContainer beanContainer;
	private final Map<String, Class> registeredDefinition = new HashMap<String, Class>();

	public Injector() {
		for (Class iterable_element : getClasses()) {
			register(iterable_element);
		}
	}

	public Map<String, Class> getRegistered() {
		return registeredDefinition;
	}

	public <T> T inject(final T bean) {
		Class<? extends Object> rootClass = bean.getClass();
		injectBySelectedClass(bean, rootClass);
		return bean;
	}

	@SuppressWarnings("unchecked")
	private <T> void injectBySelectedClass(final T bean,
			Class<? extends Object> rootClass) {
		Class supperClass = rootClass.getSuperclass();

		for (final Field declaredField : rootClass.getDeclaredFields()) {
			final Class<?> propertyType = declaredField.getType();
			Wire wireAnnotation = declaredField.getAnnotation(Wire.class);

			if (propertyType != null && wireAnnotation != null) {
				if (!wireAnnotation.name().isEmpty()) {
					injectField(bean, declaredField, wireAnnotation.name());
				} else if(isRegistered(propertyType)) {
					injectField(bean, declaredField);					
				}
				
			}
		}

		if (supperClass != null) {
			injectBySelectedClass(bean, supperClass);
		}
	}

	private <T> void injectField(final T bean, final Field field, String beanName) {
		try {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			// checkAnnotation add in near future:D
			// @Inject("myOwnNameBean")
			// now its working using just Class type to find first in kind.
			
			boolean isFieldNotSetAlready = (field.get(bean) == null);
			if (isFieldNotSetAlready) {
				Object beanToInsert = null; 
				if (beanName != null) {
					beanToInsert = beanContainer.getBean(beanName, field.getType());	
				} else {
					beanToInsert = beanContainer.getBean(field.getType());
				}
				field.set(bean, beanToInsert);
			}
			field.setAccessible(accessible);

		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private <T> void injectField(final T bean, final Field field) {
		injectField(bean, field, null);
	}

	@SuppressWarnings("unchecked")
	boolean isRegistered(final Class propertyType) {
		final Collection<Class> values = registeredDefinition.values();
		for (final Class registeredClass : values) {
			if (propertyType.isAssignableFrom(registeredClass)) {
				return true;
			}
		}
		return false;
	}

	public void setContainer(final BeanContainer beanContainer) {
		this.beanContainer = beanContainer;
	}

	public void register(final Class<? extends Object> clazz) {
		this.register(clazz.getName(), clazz);
	}

	protected void register(final String string, final Class clazz) {
		registeredDefinition.put(string, clazz);
	}

	public Class[] getClasses() {
		return new Class[] {};
	}
}