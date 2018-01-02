package pl.tahona.di.scanner;

import java.lang.annotation.Annotation;

public interface ScannerDefinition<A extends Annotation> {
    Class<A> getAnnotation();
    String getBeanName(A aClass);
}
