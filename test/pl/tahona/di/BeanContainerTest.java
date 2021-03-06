package pl.tahona.di;

import pl.tahona.di.annotation.Bean;
import pl.tahona.di.annotation.Init;
import pl.tahona.di.annotation.Wire;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pl.tahona.di.inject.InjectBeanException;
import pl.tahona.di.scan.beans.TestNew;
import pl.tahona.di.sameBeans.*;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

@RunWith(value = JUnit4.class)
public class BeanContainerTest {

    static class SimpleBean {
    }

    private static class RootTestBean {
        @Wire
        private TestBean innerBean;
    }

    static class TestBean {
        @Wire
        SimpleBean innerBean;

        private String test = "test";
    }

    @Test(expected = InjectBeanException.class)
    public void shouldThrowExceptionWhenNoBeanToWire() {
        // g
        final Injector injector = new Injector();
        injector.register(TestBean.class);

        // w
        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();
    }

    @Test
    public void testSecondLevelBeanCreation() {
        // g
        final Injector injector = new Injector();
        injector.register(TestBean.class);
        injector.register(SimpleBean.class);

        // w
        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // t
        final TestBean bean = beanContainer.getBean(TestBean.class);

        assertNotNull(bean);
        assertTrue(bean.innerBean instanceof SimpleBean);
        assertEquals("test", bean.test);

    }

    @Test
    public void testThirdLevelBeanCreation() {
        // g
        final Injector injector = new Injector();
        injector.register(TestBean.class);
        injector.register(SimpleBean.class);
        injector.register(RootTestBean.class);

        // w
        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // t
        final RootTestBean bean = beanContainer.getBean(RootTestBean.class);

        assertNotNull(bean);
        assertTrue(bean.innerBean.innerBean instanceof SimpleBean);
        assertEquals("test", bean.innerBean.test);

    }

    static class LoopTestBean {
        @Wire
        private LoopTestBean child;
    }

    @Test
    public void testLoopBeanCreation() {
        // g
        final Injector injector = new Injector();
        injector.register(LoopTestBean.class);

        // w
        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // t
        final LoopTestBean bean = beanContainer.getBean(LoopTestBean.class);

        assertNotNull(bean);
        assertNotNull(bean.child.child);
        assertTrue(bean.child instanceof LoopTestBean);
    }

    @Test
    public void testSimpleInjectBean() {
        // g
        final Injector injector = new Injector();
        injector.register(SimpleBean.class);

        // w
        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // t
        final TestBean bean = injector.inject(new TestBean());

        assertNotNull(bean);
        assertNotNull(bean.innerBean);
    }

    @Test
    public void testRegisterByName() {
        // g
        final Injector injector = new Injector();
        injector.register("InnerTestName", SimpleBean.class);
        injector.register("Bean", TestBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final TestBean bean = (TestBean) beanContainer.getBean("Bean");
        final TestBean bean2 = beanContainer.getBean("Bean", TestBean.class);

        // t
        assertNotNull(bean);
        assertNotNull(bean.innerBean);
        assertNotNull(bean2);
        assertNotNull(bean2.innerBean);
    }

    public static class C1 {
        @Wire
        TestBean tBean;
    }

    @Test
    public void testAddInitializedBean() {
        // g
        final Injector injector = new Injector();
        injector.register("InnerTestName", SimpleBean.class);
        injector.register(C1.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        beanContainer.addBean(new TestBean());
        beanContainer.addBean("Bean", new TestBean());
        beanContainer.initialize();

        // t
        final TestBean bean = (TestBean) beanContainer.getBean("Bean");
        final TestBean bean2 = beanContainer.getBean(TestBean.class);

        assertNotNull(bean);
        assertNotNull(bean.innerBean);
        assertNotNull(bean2);
        assertNotNull(bean2.innerBean);
        assertNotNull(beanContainer.getBean(C1.class).tBean);
    }

    private static class AnnotatedBean {
        private boolean test = false;

        @Init
        private void inits() {
            test = true;
        }
    }

    @Test
    public void testInitAnnotationOnBean() {
        // g
        final Injector injector = new Injector();
        injector.register("InnerTestName", AnnotatedBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final AnnotatedBean bean = beanContainer.getBean(AnnotatedBean.class);

        // t
        assertNotNull(bean);
        assertEquals(true, bean.test);
    }

    @Test
    public void testReplaceBean() {
        // g
        final Injector injector = new Injector();
        injector.register("InnerTestName", TestBean.class); // A
        injector.register(RootTestBean.class);// B
        injector.register(SimpleBean.class);// C

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        //new A
        final RootTestBean newA = new RootTestBean();
        final TestBean testBean = new TestBean();
        newA.innerBean = testBean;
        newA.innerBean.test = "Nowy Inner Bean";

        //newB
        final TestBean newB = new TestBean();
        newB.test = "Replaced Bean";

        // w
        beanContainer.replaceBean(newA);
        beanContainer.replaceBean("InnerTestName", newB);

        // t
        final RootTestBean root = beanContainer.getBean(RootTestBean.class);
        final TestBean bean = beanContainer.getBean(TestBean.class);

        assertNotNull(root);
        assertEquals("Nowy Inner Bean", root.innerBean.test);
        assertTrue(testBean == root.innerBean);

        assertNotNull(bean);
        assertEquals("Replaced Bean", bean.test);
    }

    @Test
    public void testReplaceBeanByName() {
        // g
        final Injector injector = new Injector();
        injector.register(SimpleBean.class);
        injector.register("InnerTestName", TestBean.class);
        injector.register(RootTestBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        final RootTestBean newA = new RootTestBean();
        newA.innerBean = new TestBean();
        newA.innerBean.test = "Nowy Inner Bean";

        // w
        beanContainer.replaceBean(newA);

        // t
        final RootTestBean root = beanContainer.getBean(RootTestBean.class);

        assertNotNull(root);
        assertEquals("Nowy Inner Bean", root.innerBean.test);
    }

    @Bean(scope = BeanScope.LOCAL)
    static class AnnotationScopeLocalBean {

        private String test = "spoko";

        @Wire
        TestBean bean;
    }

    @Test
    public void testAnnotationScopeLocalBean() {
        // g
        final Injector injector = new Injector();
        injector.register(AnnotationScopeLocalBean.class);
        injector.register(SimpleBean.class);
        injector.register(TestBean.class);
        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final AnnotationScopeLocalBean xz = beanContainer.getBean(AnnotationScopeLocalBean.class);
        xz.test = "inny text";

        // t
        final AnnotationScopeLocalBean root = beanContainer.getBean(AnnotationScopeLocalBean.class);

        assertNotNull(root);
        assertNotNull(root.bean);
        assertEquals("spoko", root.test);
    }

    static class TestTestBean extends TestBean {
    }

    static class TestIheritance extends TestBean {
        @Wire
        AnnotationScopeLocalBean bean;

        @Wire
        TestInterface interfacel;
    }

    private static class Test2LEvelIheritance extends TestIheritance {
    }

    interface TestInterface {

    }

    private static class TestInterfaceClass implements TestInterface {

    }

    @Test
    public void testInheritanceBean() {
        // g
        final Injector injector = new Injector();
        injector.register(AnnotationScopeLocalBean.class);
        injector.register(SimpleBean.class);
        injector.register(TestInterfaceClass.class);
        // injector.register(TestBean.class);
        injector.register(TestIheritance.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final TestIheritance root = beanContainer.getBean(TestIheritance.class);

        // t

        assertNotNull(root);
        assertNotNull(root.interfacel);
        assertNotNull(root.bean);
        assertNotNull(root.innerBean);
        assertEquals("spoko", root.bean.test);
    }

    @Test
    public void shouldReturnBeansBasedOnClass() {
        // g
        final Injector injector = new Injector();
        injector.register(SimpleBean.class);
        injector.register(TestTestBean.class);
        injector.register(TestBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final TestTestBean testIheritance = beanContainer.getBean(TestTestBean.class);
        final TestBean testBean = beanContainer.getBean(TestBean.class);

        // t

        assertNotNull(testBean);
        assertNotNull(testIheritance);
        assertFalse(testIheritance == testBean);
    }

    @Test
    public void testInheritanceSecondLevelBean() {
        // g
        final Injector injector = new Injector();
        injector.register(AnnotationScopeLocalBean.class);
        injector.register(SimpleBean.class);
        injector.register(TestInterfaceClass.class);
        // injector.register(TestBean.class);
        injector.register(Test2LEvelIheritance.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final Test2LEvelIheritance root = beanContainer.getBean(Test2LEvelIheritance.class);

        // t
        assertNotNull(root);
        assertNotNull(root.bean);
        assertNotNull(root.innerBean);
        assertEquals("spoko", root.bean.test);
    }

    public static class WithoutWireBean {
        @Wire
        TestBean testBean;
        TestBean testBeanWithoutWire;
    }

    @Test
    public void testShouldNotInjectWithoutWireAnnotation() {
        // g
        final Injector injector = new Injector();
        injector.register(WithoutWireBean.class);
        injector.register(SimpleBean.class);
        injector.register(TestBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final WithoutWireBean root = beanContainer.getBean(WithoutWireBean.class);

        // t
        assertNotNull(root);
        assertNotNull(root.testBean);
        assertNull(root.testBeanWithoutWire);
    }

    public static class WithWireNameBean {
        @Wire(name = "test")
        TestBean testBean;
        @Wire
        TestBean test;
    }

    @Test
    public void testShouldWireByBeanByName() {
        // g
        final Injector injector = new Injector();
        injector.register(AnnotationScopeLocalBean.class);
        injector.register(TestInterfaceClass.class);
        injector.register(WithoutWireBean.class);
        injector.register(TestBean.class);
        injector.register(SimpleBean.class);
        injector.register("test", TestIheritance.class);

        final BeanContainer beanContainer = new BeanContainer(injector);
        beanContainer.initialize();

        // w
        final WithoutWireBean root = beanContainer.getBean(WithoutWireBean.class);

        // t
        assertNotNull(root);
        assertTrue(root.testBean instanceof TestBean);
        assertFalse(root.testBeanWithoutWire instanceof TestBean);
    }

    // 20.12.13 - 23ms before add conqurency
    // 13.05.16 - 12ms  - szybszy sprz�t jest teraz
    // 1.1.18 - 100ms  - wolniejszy sprzęt oraz więcej refleksji
    //
    @Test
    public void testSpeedTime() {
        // g
        final Injector injector = new Injector();

        injector.register(AnnotationScopeLocalBean.class);
        injector.register(SimpleBean.class);
        injector.register(TestBean.class);
        injector.register(TestIheritance.class);
        injector.register(TestInterfaceClass.class);
        injector.register(Test2LEvelIheritance.class);
        injector.register(LoopTestBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        final long time = System.currentTimeMillis();
        beanContainer.initialize();
        final long time2 = System.currentTimeMillis();

        // t
        final long timeSpend = time2 - time;
        System.err.println(timeSpend);
        assertTrue(timeSpend < 120);

    }

    public static class InjectConstructorBean {

        @Wire
        private WireConstructorBean wireConstructorBean;

        private final SimpleBean simpleBean;

        public InjectConstructorBean(final SimpleBean simpleBean) {
            this.simpleBean = simpleBean;
        }
    }

    @Test
    public void shouldInjectToConstructor() {
        // g
        final Injector injector = new Injector();

        injector.register(WireConstructorBean.class);
        injector.register(InjectConstructorBean.class);
        injector.register(SimpleBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        beanContainer.initialize();

        // t
        final InjectConstructorBean result = beanContainer.getBean(InjectConstructorBean.class);
        assertNotNull(result.simpleBean);
    }

    public static class WireConstructorBean {
        @Wire
        InjectConstructorBean bean;

    }

    @Test
    public void shouldWireConstructorBeanToSimple() {
        // g
        final Injector injector = new Injector();

        injector.register(InjectConstructorBean.class);
        injector.register(SimpleBean.class);
        injector.register(WireConstructorBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        beanContainer.initialize();

        // t
        final InjectConstructorBean result = beanContainer.getBean(InjectConstructorBean.class);
        final WireConstructorBean w = beanContainer.getBean(WireConstructorBean.class);
        assertNotNull(result.simpleBean);
        assertNotNull(result.wireConstructorBean);
        assertNotNull(w.bean);
    }

    public static class InjectConstructorBeanSimple {

        private final SimpleBean simpleBean;

        public InjectConstructorBeanSimple(final SimpleBean simpleBean) {
            this.simpleBean = simpleBean;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenNoBeanToWireConstructorBean() {
        // g
        final Injector injector = new Injector();

        injector.register(InjectConstructorBeanSimple.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        beanContainer.initialize();
    }

    public static class InjectConstructorWithInitBean {
        private final SimpleBean simpleBean;
        private boolean invokedInit;

        public InjectConstructorWithInitBean(final SimpleBean simpleBean) {
            this.simpleBean = simpleBean;
        }

        @Init
        void init() {
            this.invokedInit = true;
        }
    }

    @Test
    public void shouldInvokeInitMethodWhenConstructorBeanInjected() {
        // g
        final Injector injector = new Injector();

        injector.register(InjectConstructorWithInitBean.class);
        injector.register(SimpleBean.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        beanContainer.initialize();

        // t
        final InjectConstructorWithInitBean result = beanContainer.getBean(InjectConstructorWithInitBean.class);
        assertNotNull(result.simpleBean);
        assertTrue(result.invokedInit);
    }

    /**
     * Fix for Bug with overling creator
     */
    @Test
    public void shouldCreateThreeBeansWithSameConstructorDependency() {
        // g
        final Injector injector = new Injector();

        injector.register(BeanA.class);
        injector.register(BeanC.class);
        injector.register(BeanB.class);
        injector.register(BeanD.class);
        injector.register(BeanE.class);
        injector.register(BeanF.class);

        injector.register(TestNew.class);

        final BeanContainer beanContainer = new BeanContainer(injector);

        // w
        beanContainer.initialize();

        // t
        final BeanA resultA = beanContainer.getBean(BeanA.class);
        assertNotNull(resultA);
        assertNotNull(resultA.getTestNew());

        final BeanB resultB = beanContainer.getBean(BeanB.class);
        assertNotNull(resultB);
        assertNotNull(resultB.getBeanA());

        final BeanF beanF = beanContainer.getBean(BeanF.class);
        assertNotNull(beanF);
        assertNotNull(beanF.getBeanA());
    }

}
