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
        this.addAnnotation(new SimpleInjectDefinition<>(Wire.class, Wire::name));

        for (final Class classes : getClasses()) {
            register(classes);
        }
    }

    public <A extends Annotation> void addAnnotation(final InjectDefinition<A> definition) {
        annotationSet.add(definition);
    }

    Map<String, Class> getRegistered() {
        return registeredDefinition;
    }

    <T> T inject(final T bean) {
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
                        Annotation annotation = declaredField.getAnnotation(definition.getAnnotationType());
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
            final boolean accessible = field.isAccessible();
            field.setAccessible(true);
            // checkAnnotation add in near future:D
            // @Inject("myOwnNameBean")
            // now its working using just Class type to find first in kind.

            final boolean isFieldNotSetAlready = (field.get(bean) == null);
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
