package pl.tahona.di.sameBeans;

import pl.tahona.di.annotation.Bean;
import pl.tahona.di.scan.beans.TestNew;

@Bean
public class BeanA {
    private final TestNew testNew;

    public BeanA(final TestNew testNew) {
        this.testNew = testNew;
    }

    public TestNew getTestNew() {
        return testNew;
    }
}
