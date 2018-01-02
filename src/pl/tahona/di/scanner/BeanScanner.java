package pl.tahona.di.scanner;

import pl.tahona.di.ReflectionUtils;
import pl.tahona.di.annotation.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeanScanner {

    private final String[] packages;

    private final BeanNameProvider nameProvider = new BeanNameProvider();
    private final List<ScannerDefinition> definitions = new ArrayList<>();

    public BeanScanner(final String... packages) {
        this.packages = packages;

        addDefinition(new SimpleScannerDefinition<>(Bean.class, Bean::value));
    }

    public Map<String, Class> scan() {
        final List<Class> list = new ArrayList<>();
        for (final String aPackage : packages) {
            list.addAll(getClass(aPackage));
        }

        return list.stream()
                .filter(this::isSupportedAnnotationExist)
                .collect(Collectors.toMap(this::getName, x -> x));
    }

    private boolean isSupportedAnnotationExist(final Class aClass) {
        return definitions.stream()
                .map(def -> aClass.getAnnotation(def.getAnnotation()))
                .anyMatch(this::isNotNull);
    }

    private boolean isNotNull(final Object annotation) {
        return annotation != null;
    }

    private String getName(final Class aClass) {
        final ScannerDefinition definition = definitions.stream()
                .filter(def -> aClass.getAnnotation(def.getAnnotation()) != null)
                .filter(this::isNotNull)
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        final String beanName = definition.getBeanName(aClass.getAnnotation(definition.getAnnotation()));

        if (beanName != null && !beanName.isEmpty()) {
            return beanName;
        }

        return nameProvider.getBeanName(aClass);
    }

    private List<Class> getClass(final String aPackage) {
        try {
            return ReflectionUtils.getClasses(aPackage);
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Package scan not working");
        }
    }

    public void addDefinition(final ScannerDefinition definition) {
        this.definitions.add(definition);
    }

    public void addDefinitions(final Collection<ScannerDefinition> definitions) {
        this.definitions.addAll(definitions);
    }
}
