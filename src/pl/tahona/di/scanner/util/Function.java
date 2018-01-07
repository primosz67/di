package pl.tahona.di.scanner.util;

public interface Function<A, T> {
    T apply(A aClass);
}
