package pl.tahona.di.scan.beans;

import pl.tahona.di.annotation.Bean;

@Bean
public class BeanRepositoryWrapper {

    private BeanRepositoryInterface repositoryInterface;
    private BeanComponentAnnotated beanComponentAnnotated;
    private final BeanServiceAnnotated serviceAnnotated;

    public BeanRepositoryWrapper(final BeanRepositoryInterface repositoryInterface, BeanComponentAnnotated beanComponentAnnotated, BeanServiceAnnotated serviceAnnotated) {
        this.repositoryInterface = repositoryInterface;
        this.beanComponentAnnotated = beanComponentAnnotated;
        this.serviceAnnotated = serviceAnnotated;
    }

    public BeanRepositoryInterface getRepositoryInterface() {
        return repositoryInterface;
    }

    public void setRepositoryInterface(BeanRepositoryInterface repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
    }

    public BeanComponentAnnotated getBeanComponentAnnotated() {
        return beanComponentAnnotated;
    }

    public void setBeanComponentAnnotated(BeanComponentAnnotated beanComponentAnnotated) {
        this.beanComponentAnnotated = beanComponentAnnotated;
    }

    public BeanServiceAnnotated getServiceAnnotated() {
        return serviceAnnotated;
    }
}
