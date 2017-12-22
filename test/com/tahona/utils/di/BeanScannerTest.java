package com.tahona.utils.di;

import com.tahona.utils.di.scan.beans.BeanComponentAnnotated;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class BeanScannerTest {
    @Test
    public void scan() throws Exception {

        final BeanScanner beanScanner = new BeanScanner("com.tahona.utils.di.scan");
        final Map<String, Class> scan = beanScanner.scan();

        Assert.assertEquals(3, scan.size());
    }

    @Test
    public void buildBeans() throws Exception {

        final BeanScanner beanScanner = new BeanScanner("com.tahona.utils.di.scan");
        final Map<String, Class> classes = beanScanner.scan();

        final Injector injector = new Injector();
        injector.addAll(classes);

        final BeanContainer container = new BeanContainer(injector);
        container.initialize();

        assertNotNull(container.getBean(BeanComponentAnnotated.class));
        assertNotNull(container.getBean("testBeanName"));
        assertNotNull(container.getBean("testBeanName", BeanComponentAnnotated.class));

    }
}