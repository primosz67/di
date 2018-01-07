package pl.tahona.di.scanner;

import pl.tahona.di.ReflectionUtils;

import java.io.IOException;
import java.util.Collection;

public class BasicFinder implements BeanFinder {
    @Override
    public Collection<Class> findClasses(final String aPackage) {
        try {
            return ReflectionUtils.getClasses(aPackage);
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Package scan not working");
        }
    }

}
