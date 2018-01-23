package pl.tahona.di;

import org.junit.Assert;
import org.junit.Test;
import pl.tahona.di.scan.beans.BeanComponentAnnotated;
import pl.tahona.di.scan.beans.BeanRepositoryWrapper;
import pl.tahona.di.scan.beans.TestNew;
import pl.tahona.di.scan.init.BeanRepositoryWrapperDecorator;
import pl.tahona.di.scanner.BeanScanner;
import pl.tahona.di.scanner.SimpleScannerDefinition;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BeanScannerTest {

    private static final String SCAN_PACKAGE = "pl.tahona.di.scan";

    @Test
    public void scan() throws Exception {
        final BeanScanner beanScanner = new BeanScanner(SCAN_PACKAGE);
        final Map<String, Class> scan = beanScanner.scan();

        Assert.assertEquals(6, scan.size());
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

    @Test
    public void shouldFindUsingNewAnnotation() throws Exception {

        final BeanScanner beanScanner = new BeanScanner(SCAN_PACKAGE);
        beanScanner.addDefinition(new SimpleScannerDefinition(TestInjectAnnotation.class, x -> null));
        final Map<String, Class> classes = beanScanner.scan();

        final Injector injector = new Injector();
        injector.registerAll(classes);

        final BeanContainer container = new BeanContainer(injector);
        container.initialize();

        assertNotNull(container.getBean(TestNew.class));

    }

    @Test
    public void shouldInjectInterfaceToContructor() throws Exception {
        //g
        final BeanScanner beanScanner = new BeanScanner(SCAN_PACKAGE);
        final Map<String, Class> classes = beanScanner.scan();

        final Injector injector = new Injector();
        injector.registerAll(classes);

        final BeanContainer container = new BeanContainer(injector);
        //w
        container.initialize();

        //then
        final BeanRepositoryWrapper bean = container.getBean(BeanRepositoryWrapper.class);
        assertNotNull(bean);
        assertNotNull(bean.getRepositoryInterface());
        assertNotNull(bean.getBeanComponentAnnotated());
        assertNotNull(bean.getServiceAnnotated());
        assertTrue(bean.isInit());

    }

    @Test
    public void shouldInjectInterfacesToContructorAndInvokeInitOnParent() throws Exception {
        //g
        final BeanScanner beanScanner = new BeanScanner(SCAN_PACKAGE);
        final Map<String, Class> classes = beanScanner.scan();

        final Injector injector = new Injector();
        injector.registerAll(classes);

        final BeanContainer container = new BeanContainer(injector);
        //w
        container.initialize();

        //then
        final BeanRepositoryWrapperDecorator bean = container.getBean(BeanRepositoryWrapperDecorator.class);
        assertNotNull(bean);
        assertNotNull(bean.getRepositoryInterface());
        assertNotNull(bean.getBeanComponentAnnotated());
        assertNotNull(bean.getServiceAnnotated());
        assertTrue(bean.isInit());

    }

}