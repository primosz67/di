package pl.tahona.di;

import org.junit.Assert;
import org.junit.Test;
import pl.tahona.di.scan.beans.BeanComponentAnnotated;
import pl.tahona.di.scanner.BeanScanner;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class BeanScannerTest {

    private static final String SCAN_PACKAGE = "pl.tahona.di.scan";

    @Test
    public void scan() throws Exception {

        final BeanScanner beanScanner = new BeanScanner(SCAN_PACKAGE);

        final Map<String, Class> scan = beanScanner.scan();

        Assert.assertEquals(3, scan.size());
    }

    @Test
    public void buildBeans() throws Exception {

        final BeanScanner beanScanner = new BeanScanner(SCAN_PACKAGE);
        final Map<String, Class> classes = beanScanner.scan();

        final Injector injector = new Injector();
        injector.registerAll(classes);

        final BeanContainer container = new BeanContainer(injector);
        container.initialize();

        assertNotNull(container.getBean(BeanComponentAnnotated.class));
        assertNotNull(container.getBean("testBeanName"));
        assertNotNull(container.getBean("testBeanName", BeanComponentAnnotated.class));

    }
}