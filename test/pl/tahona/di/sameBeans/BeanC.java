package pl.tahona.di.sameBeans;

import pl.tahona.di.annotation.Bean;

@Bean
public class BeanC {
    private final BeanA beanA;

    public BeanC(final BeanA testNew) {
        this.beanA = testNew;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
