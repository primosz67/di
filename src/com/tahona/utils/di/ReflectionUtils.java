package com.tahona.utils.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ReflectionUtils {

    static void invokeMethodWith(final Object bean, final Class<? extends Annotation> class1,
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

    public static List<Method> getMethods(final Object bean, final Class annotationClass) {
        return getMethods(bean, annotationClass, false);
    }

    private static List<Method> getMethods(final Object bean, final Class annotationClass, final boolean searchInInnerClasses) {
        final Class<? extends Object> beanClass = bean.getClass();
        return getMethods(bean, beanClass, annotationClass, searchInInnerClasses);
    }

    private static List<Method> getMethods(final Object bean, final Class<? extends Object> beanClass,
                                           final Class annotationClass, final boolean searchInInnerClasses) {
        final Method[] methods = beanClass.getDeclaredMethods();

        final List<Method> methodsList = new ArrayList<Method>();

        for (final Method method : methods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(annotationClass)) {
                methodsList.add(method);
            }
        }
        final Class<?> superclass = beanClass.getSuperclass();
        if (searchInInnerClasses && false == superclass.isAssignableFrom(Object.class)) {
            methodsList.addAll(getMethods(bean, superclass, annotationClass, searchInInnerClasses));
        }

        return methodsList;
    }

    public static void invokeMethodWith(final Object bean, final Method method, final Object... objects) {
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

    public static <T> T newInstance(final Class sc, final Class[] cls, final Object... obj) {
        try {

            final Constructor constructor;
            constructor = sc.getDeclaredConstructor(cls);
            return (T) constructor.newInstance(obj);
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Something went wrong");
    }

    public static <T> T newInstance(final Class sc) {
        try {

            final Constructor constructor = sc.getDeclaredConstructor(null);
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Something went wrong");
    }

    private static Class[] getTypes(final Object[] obj) {
        final Set<Class> c = new HashSet<Class>();
        for (final Object object : obj) {
            c.add(object.getClass());
        }
        return c.toArray(new Class[]{});
    }

}
