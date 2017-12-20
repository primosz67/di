package com.tahona.utils.di;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dependency injection
 * @author primosz67
 *
 */
public class BeanContainer {

	private BeanContainerHelper helper = new BeanContainerHelper();

	private final Map<String, Object> beanList = new ConcurrentHashMap<String, Object>();

	private final Injector injector;
	private boolean initialized;

	public BeanContainer(final Injector injector) {
		this.injector = injector;
		injector.setContainer(this);
	}
	
	public void initialize() {
		if (initialized) {
			throw new UnsupportedOperationException("Cannot initialize two times!");
		}
		initialized = true;

		final Map<String, Class> registered = injector.getRegistered();
		createBeans(registered);
		injectAllBean(injector);
		runInitAnnotation();
	}

	private void injectAllBean(final Injector injector) {
		for (final Object bean : beanList.values()) {
			injector.inject(bean);
		}
	}

	private void createBeans(final Map<String, Class> registered) {

		// addedBean
		final Set<String> registeredBeansKey = new HashSet<String>(beanList.keySet());

		for (final String key : registered.keySet()) {
			final Object object = helper.createObject(registered.get(key));
			addBean(key, object);
		}

		// updateAddedBean
		updateInjectorRegistry(registeredBeansKey, injector);
	}

	private void updateInjectorRegistry(final Set<String> registeredBeansKey, final Injector injectRegistry) {
		for (final String addedBeanName : registeredBeansKey) {
			injectRegistry.register(addedBeanName, beanList.get(addedBeanName).getClass());
		}
	}

	public void addBean(final String key, final Object object) {
		if (beanList.containsKey(key)) {
			throw new IllegalArgumentException("Bean with name " + key + " allready registered");
		}
		beanList.put(key, object);
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(final String name, final Class<T> clazz) {
		Object bean = this.getBean(name);
		if (bean == null) {
			bean = this.getBean(clazz);
		} 
		return (T) bean;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(final Class<T> clazz) {
		if (helper.isLocal(clazz)) {
			return (T) injector.inject(helper.createObject(clazz));
		} else {
			return helper.findByType(beanList, clazz);
		}
	}

	public Object getBean(final String beanName) {
		if (beanName != null && beanList.containsKey(beanName) ) {
			Object object = beanList.get(beanName);
			Class<? extends Object> class1 = object.getClass();
			if (helper.isLocal(class1)) {
				return injector.inject(helper.createObject(class1));
			} else {
				return beanList.get(beanName);
			}
			
		} else {
			return null;
		}
	}

	public void addBean(final Object obj) {
		this.addBean(helper.provideBeanName(obj), obj);
	}

	private void runInitAnnotation() {
		for (final Object bean : beanList.values()) {
			ReflectionUtils.invokeMethodWith(bean, Init.class);
		}
	}

	/**
	 *  Add bean to container or replace, with registering in injector.
	 *  
	 *  Note: the object will not be auto injected to already initialized beans, 
	 *  	but will be injected by Injector in future injections.
	 *  	- getBean also will work;
	 *  
	 * @param final String providedBeanName - name to replace
	 * @param bean Object
	 */
	public void replaceBean(String providedBeanName, final Object bean) {
		final  Object initializedBean = this.getBean(providedBeanName);
		if (initializedBean == null && helper.hasNotType(bean.getClass(), beanList)) {
			addAndRegisterNewBean(providedBeanName, bean);
		} else {
			beanList.put(providedBeanName, bean);
		}
	}

	private void addAndRegisterNewBean(String providedBeanName, Object bean) {
		addBean(providedBeanName, bean);
		final HashSet<String> registeredBeansKey = new HashSet<String>();
		registeredBeansKey.add(providedBeanName);
		updateInjectorRegistry(registeredBeansKey, injector);
	}

	public void replaceBean(final Object bean) {
		replaceBean(helper.provideBeanName(bean), bean);
	}

	public Injector getInjector() {
		return injector;
	}

	public void clear() {
		beanList.clear();
		injector.getRegistered().clear();
	}
}
