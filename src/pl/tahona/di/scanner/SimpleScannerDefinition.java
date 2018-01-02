package pl.tahona.di.scanner;

import java.lang.annotation.Annotation;
import java.util.function.Function;

public class SimpleScannerDefinition<A extends Annotation> implements ScannerDefinition<A> {

    private final Class<A> annotation;
    private final Function<A, String> nameProvider;

    public SimpleScannerDefinition(final Class<A> annotation, Function<A, String> nameProvider) {
        this.annotation = annotation;
        this.nameProvider = nameProvider;
    }

    @Override
    public Class<A> getAnnotation() {
        return this.annotation;
    }

    @Override
    public String getBeanName(A aClass) {
        return nameProvider.apply(aClass);
    }

}
