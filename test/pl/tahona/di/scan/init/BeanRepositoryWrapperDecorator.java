package pl.tahona.di.scan.init;

import pl.tahona.di.annotation.Bean;
import pl.tahona.di.scan.beans.BeanComponentAnnotated;
import pl.tahona.di.scan.beans.BeanRepositoryInterface;
import pl.tahona.di.scan.beans.BeanRepositoryWrapper;
import pl.tahona.di.scan.beans.BeanServiceAnnotated;

@Bean
public class BeanRepositoryWrapperDecorator extends BeanRepositoryWrapper {
    public BeanRepositoryWrapperDecorator(final BeanRepositoryInterface repositoryInterface, final BeanComponentAnnotated beanComponentAnnotated, final BeanServiceAnnotated serviceAnnotated) {
        super(repositoryInterface, beanComponentAnnotated, serviceAnnotated);
    }
}
