package pl.tahona.di.scanner;

class BeanNameProvider {
    String getBeanName(final Class aClass) {
        return  aClass.getSimpleName();
    }
}
