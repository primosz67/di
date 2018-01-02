package pl.tahona.di.scanner;

import java.lang.annotation.Annotation;

interface ScannerDefinition<A extends Annotation> {
    Class<A> getAnnotation();
    String getBeanName(A aClass);
}
