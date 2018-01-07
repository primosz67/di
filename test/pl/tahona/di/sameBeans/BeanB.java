package pl.tahona.di.sameBeans;

import pl.tahona.di.annotation.Bean;

@Bean
public class BeanB {
    private final BeanA beanA;

    public BeanB(final BeanA testNew) {
        this.beanA = testNew;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
