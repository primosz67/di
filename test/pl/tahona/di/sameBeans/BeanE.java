package pl.tahona.di.sameBeans;

import pl.tahona.di.annotation.Bean;

@Bean
public class BeanE {
    private final BeanA beanA;

    public BeanE(final BeanA testNew) {
        this.beanA = testNew;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
