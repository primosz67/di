package pl.tahona.di;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import pl.tahona.di.annotation.Init;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        final Map<Class, Set<BeanCreator>> creatorsRegistry = buildCreatorsMap(creatorsList);

        for (final BeanCreator beanCreator : creatorsList) {
            addBeanByCreator(creatorsRegistry, beanCreator);
        }

        checkMissingBeans(creatorsList);
    }

    private List<BeanCreator> buildCreators(final Map<String, Class> all) {
        return FluentIterable.from(all.entrySet())
                .filter(Predicates.not(predicateNoConstruct()))
                .transform(entry -> {
                    final String beanName = entry.getKey();
                    final Class classDefinition = entry.getValue();
                    return new BeanCreator(beanName, classDefinition, getConstructorBeans(classDefinition));
                })
                .toList();
    }

    private Map<Class, Set<BeanCreator>> buildCreatorsMap(final List<BeanCreator> creatorsList) {
        final Map<Class, Set<BeanCreator>> creatorMap = new HashMap<>();

        for (final BeanCreator creator : creatorsList) {
            final List<Class<?>> clazz = Arrays.asList(creator.getConstructorBeans());

            for (final Class<?> c : clazz) {
                final Set<BeanCreator> collection = Optional.fromNullable(creatorMap.get(c))
                        .or(HashSet::new);

                collection.add(creator);
                creatorMap.put(c, collection);
            }
        }

        return creatorMap;
    }

    private void checkMissingBeans(final List<BeanCreator> creatorsList) {
        final List<BeanCreator> notFilled = FluentIterable.from(creatorsList)
                .filter(x -> !x.isCreated())
                .toList();

        if (!notFilled.isEmpty()) {
            final ImmutableList<String> strings = FluentIterable.from(notFilled)
                    .transform(creator -> {
                        final Class[] constructorBeans = creator.getConstructorBeans();

                        final List<String> missingClasses = FluentIterable.from(Arrays.asList(constructorBeans))
                                .filter(z -> getBean(z) == null)
                                .transform(c->c.toString())
                                .toList();

                        return creator.getBeanName() + ": " + missingClasses.toString() + ", ";
                    }).toList();
            final String missingBeans = Joiner.on(" ").join(strings);

            throw new IllegalStateException("Missing beans (" + missingBeans + ")");
        }
    }

    private void addBeanByCreator(final Map<Class, Set<BeanCreator>> creatorRegistry, final BeanCreator beanCreator) {
        if (!beanCreator.isCreated()) {

            final List<Class> clazz = Arrays.asList(beanCreator.getConstructorBeans());
            final ImmutableList<Object> beans = FluentIterable.from(clazz)
                    .transform(this::getBean)
                    .filter(x->x!=null)
                    .toList();

            final Object o = beanCreator.create(beans);

            if (o != null) {
                addBean(beanCreator.getBeanName(), o);

                final Set<Class> classes = ReflectionUtils.getClassesOfClass(o.getClass());

                final ImmutableList<BeanCreator> beanCreators = FluentIterable.from(classes)
                        .filter(createdBeanClass -> creatorRegistry.get(createdBeanClass) != null)
                        .transformAndConcat(creatorRegistry::get)
                        .toList();

                for (final BeanCreator beanCreatorToInvoke : beanCreators) {
                    addBeanByCreator(creatorRegistry, beanCreatorToInvoke);
                }
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
        final ImmutableList<Map.Entry<String, Class>> entries = FluentIterable.from(all.entrySet())
                .filter(predicateNoConstruct())
                .toList();

        final Map<String, Class> registered = ImmutableMap.copyOf(entries);

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
            return helper.findOneByType(beanList, clazz);
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
