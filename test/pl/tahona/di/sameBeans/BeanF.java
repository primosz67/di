package pl.tahona.di.sameBeans;

import pl.tahona.di.annotation.Bean;

@Bean
public class BeanF {
    private final BeanA beanA;

    public BeanF(final BeanA testNew) {
        this.beanA = testNew;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
