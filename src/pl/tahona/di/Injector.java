package pl.tahona.di;

import pl.tahona.di.annotation.Wire;
import pl.tahona.di.inject.InjectBeanException;
import pl.tahona.di.inject.InjectDefinition;
import pl.tahona.di.inject.SimpleInjectDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class Injector {

    private BeanContainer beanContainer;
    private final Map<String, Class> registeredDefinition = new HashMap<>();
    private final Set<InjectDefinition> annotationSet = new HashSet<>();

    public Injector() {
        this.addDefinition(new SimpleInjectDefinition<>(Wire.class, Wire::name));

        for (final Class classes : getClasses()) {
            register(classes);
        }
    }

    public void addDefinition(final InjectDefinition definition) {
        annotationSet.add(definition);
    }

    public void addDefinitions(final Set<InjectDefinition> definitions) {
        annotationSet.addAll(definitions);
    }

    Map<String, Class> getRegistered() {
        return registeredDefinition;
    }

    public <T> T inject(final T bean) {
        final Class<? extends Object> rootClass = bean.getClass();


        injectBySelectedClass(bean, rootClass);
        return bean;
    }

    @SuppressWarnings("unchecked")
    private <T> void injectBySelectedClass(final T bean,
                                           final Class<? extends Object> rootClass) {

        final Class supperClass = rootClass.getSuperclass();

        for (final Field declaredField : rootClass.getDeclaredFields()) {
            final Class<?> propertyType = declaredField.getType();

            if (propertyType != null) {
                final Optional<InjectDefinition> optionalDef = getDefinition(declaredField);

                if (optionalDef.isPresent()) {
                    final String name = optionalDef.map(definition -> {
                        final Annotation annotation = declaredField.getAnnotation(definition.getAnnotationType());
                        return definition.getBeanName(annotation);
                    }).orElse("");

                    if (!name.isEmpty()) {
                        injectField(bean, declaredField, name);
                    } else if (isRegistered(propertyType)) {
                        injectField(bean, declaredField);
                    } else {
                        throw new InjectBeanException("Cannot inject Bean for: " + declaredField.toString());
                    }
                }
            }
        }

        if (supperClass != null) {
            injectBySelectedClass(bean, supperClass);
        }
    }

    private Optional<InjectDefinition> getDefinition(final Field propertyType) {
        return this.annotationSet.stream()
                .filter(x -> propertyType.getAnnotation(x.getAnnotationType()) != null)
                .findFirst();
    }

    private <T> void injectField(final T bean, final Field field, final String beanName) {
        try {
            final boolean historyFieldState = field.isAccessible();
            field.setAccessible(true);

            final boolean isFieldNotSetAlready = (field.get(bean) == null);
            if (isFieldNotSetAlready) {
                final Object beanToInsert = getBean(field, beanName);
                field.set(bean, beanToInsert);
            }

            //set old access setup
            field.setAccessible(historyFieldState);

        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new InjectBeanException("Error when injecting: " + beanName + ". " + field.getName(), e);
        }
    }

    private Object getBean(final Field field, final String beanName) {
        if (beanName != null) {
            return beanContainer.getBean(beanName, field.getType());
        }
        return beanContainer.getBean(field.getType());
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

    public void register(final String string, final Class clazz) {
        registeredDefinition.put(string, clazz);
    }

    private Class[] getClasses() {
        return new Class[]{};
    }

    public void registerAll(final Map<String, Class> classes) {
        classes.forEach(this::register);
    }
}
