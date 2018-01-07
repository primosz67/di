package pl.tahona.di.scanner;

import java.util.Collection;

public interface BeanFinder {
    Collection<Class> findClasses(String aPackage);
}
