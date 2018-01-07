package pl.tahona.di.sameBeans;

import pl.tahona.di.annotation.Bean;

@Bean
public class BeanD {
    private final BeanA beanA;

    public BeanD(final BeanA testNew) {
        this.beanA = testNew;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
