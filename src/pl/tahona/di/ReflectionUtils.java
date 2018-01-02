package pl.tahona.di;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public final class ReflectionUtils {

    public static void invokeMethodWith(final Object bean, final Class<? extends Annotation> class1,
                                        final Object... objects) {
        final Method[] methods = bean.getClass().getDeclaredMethods();
        for (final Method method : methods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(class1)) {
                try {
                    method.invoke(bean, objects);
                } catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Method> getMethods(final Object bean, final Class annotationClass) {
        return getMethods(bean, annotationClass, false);
    }

    public static List<Method> getMethods(final Object bean, final Class annotationClass, final boolean searchInInnerClasses) {
        final Class<? extends Object> beanClass = bean.getClass();
        return getMethods(bean, beanClass, annotationClass, searchInInnerClasses);
    }

    public static List<Method> getMethods(final Object bean, final Class<? extends Object> beanClass,
                                          final Class annotationClass, final boolean searchInInnerClasses) {
        final Method[] methods = beanClass.getDeclaredMethods();

        final List<Method> methodsList = new ArrayList<>();

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
        } catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> T newInstance(final Class sc, final Class[] cls, final Object... obj) {
        try {

            final Constructor constructor = sc.getDeclaredConstructor(cls);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(obj);
        } catch (final SecurityException | InvocationTargetException | IllegalAccessException | InstantiationException | IllegalArgumentException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Something went wrong -" +
                " bean: " + sc +
                " classes: " + join(cls) +
                " objects: " + join(obj));
    }

    private static String join(final Object... cls) {
        final List<Object> classes = Arrays.asList(cls);

        final StringBuilder res = new StringBuilder();
        for (final Object aClass : classes) {
            res.append(" ");
            res.append(aClass.toString());
        }
        return res.toString();
    }

    public static <T> T newInstance(final Class sc) {
        try {

            final Constructor constructor = sc.getDeclaredConstructor(null);
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (final SecurityException | InvocationTargetException | IllegalAccessException | InstantiationException | IllegalArgumentException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Something went wrong " + sc);
    }

    private static Class[] getTypes(final Object[] obj) {
        final Set<Class> c = new HashSet<>();
        for (final Object object : obj) {
            c.add(object.getClass());
        }
        return c.toArray(new Class[]{});
    }

    public static List<Class> getClasses(final String packageName)
            throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        final String path = packageName.replace('.', '/');
        final Enumeration<URL> resources = classLoader.getResources(path);
        final List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        final ArrayList<Class> classes = new ArrayList<>();
        for (final File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(final File directory, final String packageName) throws ClassNotFoundException {
        final List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        final File[] files = directory.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static Set<Class> getClassesOfClass(Class clazz) {
            final List<Class> res = new ArrayList<>();

            do {
                res.add(clazz);

                // First, add all the interfaces implemented by this class
                final Class[] interfaces = clazz.getInterfaces();

                if (interfaces.length > 0) {
                    res.addAll(Arrays.asList(interfaces));

                    for (final Class clasInterface : interfaces) {
                        res.addAll(getClassesOfClass(clasInterface));
                    }
                }

                // Add the super class
                final Class<?> superClass = clazz.getSuperclass();

                // Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle and return
                if (superClass == null) {
                    break;
                }

                // Now inspect the superclass
                clazz = superClass;
            } while (!"java.lang.Object".equals(clazz.getCanonicalName()));

            return new HashSet<>(res);

    }
}
