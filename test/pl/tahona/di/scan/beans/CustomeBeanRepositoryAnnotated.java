package pl.tahona.di.scan.beans;

import pl.tahona.di.annotation.Bean;

@Bean()
class CustomeBeanRepositoryAnnotated implements BeanRepositoryInterface {

    private BeanRepositoryAnnotated beanRepositoryAnnotated;

    public CustomeBeanRepositoryAnnotated(BeanRepositoryAnnotated beanRepositoryAnnotated) {
        this.beanRepositoryAnnotated = beanRepositoryAnnotated;
    }

    public BeanRepositoryAnnotated getBeanRepositoryAnnotated() {
        return beanRepositoryAnnotated;
    }
}
