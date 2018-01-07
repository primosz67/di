package pl.tahona.di.inject;

import pl.tahona.di.scanner.util.Function;

import java.lang.annotation.Annotation;

public class SimpleInjectDefinition<A extends Annotation> implements InjectDefinition<A> {
    private final Class<A> aClass;
    private final Function<A, String> function;

    public SimpleInjectDefinition(final Class<A> aClass, final Function<A, String> function) {
        this.aClass = aClass;
        this.function = function;
    }

    @Override
    public Class<A> getAnnotationType() {
        return aClass;
    }

    @Override
    public String getBeanName(final A annotation) {
        return function.apply(annotation);
    }
}
