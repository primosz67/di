package pl.tahona.di.inject;

import java.lang.annotation.Annotation;

public interface InjectDefinition<A extends Annotation> {
    Class<A> getAnnotationType();
    String getBeanName(A annotation);
}
