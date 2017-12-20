package com.tahona.utils.di;

import java.util.Map;

class BeanContainerHelper {

	<T> T findByType(final Map<String, Object> beanList, final Class<T> clazz) {
		for (final Object bean : beanList.values()) {
			if (clazz.isAssignableFrom(bean.getClass())) {
				return (T) bean;
			}
		}
		return null;
	}

	// @SuppressWarnings({ "rawtypes" })
	// private Boolean isSingeltone(final Class clazz) {
	// return isBeanScopedAs(clazz, BeanScope.SINGLETON);
	// }
	@SuppressWarnings({ "rawtypes" })
	Boolean isLocal(final Class clazz) {
		return isBeanScopedAs(clazz, BeanScope.LOCAL);
	}

	@SuppressWarnings("unchecked")
	private Boolean isBeanScopedAs(final Class clazz, final BeanScope beanScope) {
		final boolean annotationPresent = clazz.isAnnotationPresent(Bean.class);
		if (annotationPresent) {
			final Bean bean = (Bean) clazz.getAnnotation(Bean.class);
			return bean.scope().equals(beanScope);
		} else {
			return false;
		}
	}

	Object createObject(final Class class1) {
		try {
			return ReflectionUtils.newInstance(class1);
		} catch (final Exception e) {
			throw new CreateInstanceException("Cannot create new Instance of class " + class1.getSimpleName() + " !", e);
		}
	}

	
	/**
	 *  Get bean name based on map.
	 * @param beanList
	 * @param bean
	 * @return
	 */
	public String getBeanName(final Map<String, Object> beanList, final Object bean) {
		String beanName = null;
		for (final String key : beanList.keySet()) {
			if (beanList.get(key).getClass().equals(bean.getClass())) {
				beanName = key;
				break;
			}
		}
		return beanName;
	}

	private boolean hastType(final Map<String, Object> beanList, final Class<? extends Object> class1) {
		final Object findByType = findByType(beanList, class1);
		return findByType != null;
	}

	boolean hasNotType(final Class<? extends Object> class1, final Map<String, Object> beanList) {
		return !this.hastType(beanList, class1);
	}

	public Object findBean(final Map<String, Object> beanList, final String beanName) {
		final Object object = beanList.get(beanName);
		if (object != null && isLocal(object.getClass())) {
			return createObject(object.getClass());
		} else {
			return object;
		}
	}

	String provideBeanName(final Object bean) {
		return bean.getClass().getName();
	}

}
