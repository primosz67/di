package pl.tahona.di.scan.beans;

import pl.tahona.di.annotation.Bean;

@Bean()
class CustomeBeanRepositoryAnnotated implements BeanRepositoryInterface {

    private final BeanRepositoryAnnotated beanRepositoryAnnotated;

    public CustomeBeanRepositoryAnnotated(final BeanRepositoryAnnotated beanRepositoryAnnotated) {
        this.beanRepositoryAnnotated = beanRepositoryAnnotated;
    }

    public BeanRepositoryAnnotated getBeanRepositoryAnnotated() {
        return beanRepositoryAnnotated;
    }
}
