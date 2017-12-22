package com.tahona.utils.di;

import com.tahona.utils.di.annotation.Component;
import com.tahona.utils.di.annotation.Repository;
import com.tahona.utils.di.annotation.Service;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class BeanScanner {

    private final String[] packages;
    private static final Collection<Class> CLASSES = Arrays.asList(
            Component.class,
            Service.class,
            Repository.class);

    private final BeanContainerHelper nameProvider = new BeanContainerHelper();

    public BeanScanner(final String... packages) {
        this.packages = packages;
    }

    public Map<String, Class> scan() {
        final List<Class> list = new ArrayList<>();
        for (final String aPackage : packages) {
            list.addAll(getClass(aPackage));

        }

        return list.stream()
                .filter(this::isSupportedAnnotationExist)
                .collect(Collectors.toMap(this::getName, x -> x));
    }

    private boolean isSupportedAnnotationExist(final Class aClass) {
        return CLASSES.stream()
                .map((Function<Class, Annotation>) aClass::getAnnotation)
                .anyMatch(this::isNotNull);
    }

    private boolean isNotNull(final Annotation annotation) {
        return annotation != null;
    }

    private String getName(final Class aClass) {
        final Optional<Annotation> annotation = CLASSES.stream()
                .map((Function<Class, Annotation>) aClass::getAnnotation)
                .filter(this::isNotNull)
                .findFirst();

        final String beanName = getAnnotationValue(annotation.orElseThrow(IllegalStateException::new));

        if (!beanName.isEmpty()) {
            return beanName;
        }

        return nameProvider.getBeanName(aClass);
    }

    private String getAnnotationValue(final Annotation annotation) {
        try {
            return (String) annotation.annotationType()
                    .getMethod("value")
                    .invoke(annotation);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Annotation do not have 'value' method", e);
        }
    }

    private List<Class> getClass(final String aPackage) {
        try {
            return ReflectionUtils.getClasses(aPackage);
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Package scan not working");
        }
    }

}
