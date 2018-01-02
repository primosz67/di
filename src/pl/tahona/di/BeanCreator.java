package pl.tahona.di;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class BeanCreator {
    private final String beanName;
    private final Class value;
    private final List<Class> constructorBeans;
    private boolean created;

    BeanCreator(final String beanName, final Class value, final Class<?>[] constructorBeans) {
        this.beanName = beanName;
        this.value = value;
        this.constructorBeans = Arrays.asList(constructorBeans);
    }

    public String getBeanName() {
        return beanName;
    }

    public Class getValue() {
        return value;
    }

    public Class<?>[] getConstructorBeans() {
        return constructorBeans.toArray(new Class[]{});
    }

    Object create(final List<Object> beans) {
        if (beans.size() == constructorBeans.size()) {
            created = true;
            return ReflectionUtils.newInstance(value, getConstructorBeans(), sort(beans));
        }
        return null;
    }

    private Object[] sort(final List<Object> beans) {
        beans.sort(Comparator.comparingInt(c -> constructorBeans.indexOf(c.getClass())));
        return beans.toArray();
    }

    public boolean isCreated() {
        return created;
    }
}
