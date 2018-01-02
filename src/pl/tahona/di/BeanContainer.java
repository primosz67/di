package pl.tahona.di;

import pl.tahona.di.annotation.Init;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeanContainer {

    private final BeanContainerHelper helper = new BeanContainerHelper();
    private final Map<String, Object> beanList = new ConcurrentHashMap<String, Object>();

    private final Injector injector;
    private boolean initialized;

    public BeanContainer(final Injector injector) {
        this.injector = injector;

        //FIXME - old mistakes :(
        injector.setContainer(this);
    }

    public void initialize() {
        if (initialized) {
            throw new UnsupportedOperationException("Cannot initialize two times!");
        }
        initialized = true;

        initNoConstructorBeans();
        initParametrizedConstructorBeans();
        updateInjectorRegistry(beanList.keySet(), injector);
        injectAllBean(injector);
        runInitAnnotation();
    }

    private void initParametrizedConstructorBeans() {
        final Map<String, Class> registeredClasses = injector.getRegistered();
        final List<BeanCreator> creatorsList = buildCreators(registeredClasses);
        final Map<Class, BeanCreator> creatorMap = buildCreatorsMap(creatorsList);

        creatorsList.forEach(c -> addBeanByCreator(creatorMap, c));

        checkMissingBeans(creatorsList);
    }

    private List<BeanCreator> buildCreators(final Map<String, Class> all) {
        return all.entrySet().stream()
                .filter(predicateNoConstruct().negate())
                .map(entry -> {
                    final String beanName = entry.getKey();
                    final Class classDefinition = entry.getValue();
                    return new BeanCreator(beanName, classDefinition, getConstructorBeans(classDefinition));
                })
                .collect(Collectors.toList());
    }

    private Map<Class, BeanCreator> buildCreatorsMap(final List<BeanCreator> creatorsList) {
        final Map<Class, BeanCreator> creatorMap = new HashMap<>();

        creatorsList.forEach(creator -> {
            final List<Class<?>> clazz = Arrays.asList(creator.getConstructorBeans());
            clazz.forEach(c -> creatorMap.put(c, creator));
        });

        return creatorMap;
    }

    private void checkMissingBeans(final List<BeanCreator> creatorsList) {
        final List<BeanCreator> notFilled = creatorsList.stream()
                .filter(x -> !x.isCreated())
                .collect(Collectors.toList());

        if (!notFilled.isEmpty()) {
            final String missingBeans = notFilled.stream()
                    .map(creator -> {
                        final Class<?>[] constructorBeans = creator.getConstructorBeans();

                        final List<String> missingClasses = Arrays.asList(constructorBeans)
                                .stream()
                                .filter(z -> getBean(z) == null)
                                .map(Class::toString)
                                .collect(Collectors.toList());

                        return creator.getBeanName() + ": " + missingClasses.toString();
                    }).reduce((s, s2) -> s + s2).orElse("");

            throw new IllegalStateException("Missing beans: " + missingBeans);
        }
    }

    private void addBeanByCreator(final Map<Class, BeanCreator> creatorMap, final BeanCreator c) {
        if (!c.isCreated()) {

            final List<Class<?>> clazz = Arrays.asList(c.getConstructorBeans());
            final List<Object> beans = clazz.stream()
                    .map(this::getBean)
                    .filter(x -> x != null)
                    .collect(Collectors.toList());

            final Object o = c.create(beans);

            if (o != null) {
                addBean(c.getBeanName(), o);

                final Set<Class> classes = ReflectionUtils.getClassesOfClass(o.getClass());

                classes.stream()
                        .filter(ccc -> creatorMap.get(ccc) != null)
                        .map(creatorMap::get)
                        .forEach(beanCreatorToInvoke ->
                                addBeanByCreator(creatorMap, beanCreatorToInvoke));
            }
        }
    }

    private Class<?>[] getConstructorBeans(final Class value) {
        final Constructor constructor = value.getDeclaredConstructors()[0];
        return constructor.getParameterTypes();
    }

    private Predicate<Map.Entry<String, Class>> predicateNoConstruct() {
        return x -> {
            try {
                x.getValue().getDeclaredConstructor(null);
                return true;
            } catch (final NoSuchMethodException e) {
                return false;
            }
        };
    }

    private void injectAllBean(final Injector injector) {
        for (final Object bean : beanList.values()) {
            injector.inject(bean);
        }
    }

    private void initNoConstructorBeans() {
        final Map<String, Class> all = injector.getRegistered();
        final Map<String, Class> registered = all.entrySet().stream()
                .filter(predicateNoConstruct())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (final String key : registered.keySet()) {
            final Object object = helper.createObject(registered.get(key));
            addBean(key, object);
        }
    }

    private void updateInjectorRegistry(final Set<String> registeredBeansKey, final Injector injectRegistry) {
        for (final String addedBeanName : registeredBeansKey) {
            injectRegistry.register(addedBeanName, beanList.get(addedBeanName).getClass());
        }
    }

    public void addBean(final String key, final Object object) {
        if (beanList.containsKey(key)) {
            throw new IllegalArgumentException("Bean with name " + key + " already registered");
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
        if (beanName != null && beanList.containsKey(beanName)) {
            final Object object = beanList.get(beanName);
            final Class<? extends Object> class1 = object.getClass();
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
     * Add bean to container or replace, with registering in injector.
     * <p>
     * Note: the object will not be auto injected to already initialized beans,
     * but will be injected by Injector in future injections.
     * - getBean also will work;
     *
     * @param providedBeanName - name to replace
     * @param bean             - Object
     */
    public void replaceBean(final String providedBeanName, final Object bean) {
        final Object initializedBean = this.getBean(providedBeanName);
        if (initializedBean == null && helper.hasNotType(bean.getClass(), beanList)) {
            addAndRegisterNewBean(providedBeanName, bean);
        } else {
            beanList.put(providedBeanName, bean);
        }
    }

    private void addAndRegisterNewBean(final String providedBeanName, final Object bean) {
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
