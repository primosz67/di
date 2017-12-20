package com.tahona.utils.di;

import junit.framework.TestCase;

import org.junit.Test;

import com.tahona.utils.di.Bean;
import com.tahona.utils.di.BeanContainer;
import com.tahona.utils.di.BeanScope;
import com.tahona.utils.di.Init;
import com.tahona.utils.di.Injector;
import com.tahona.utils.di.annotation.Wire;

public class TestBeanContainer extends TestCase {

	static class InnerTestBean {
	}

	private static class RootTestBean {
		@Wire private TestBean innerBean;
	}

	static class TestBean {
		@Wire InnerTestBean innerBean;
		private String test = "test";
	}

	public void testBeanCreation() {
		// g
		final Injector injector = new Injector();
		injector.register(TestBean.class);

		// w
		final BeanContainer beanContainer = new BeanContainer(injector);
		beanContainer.initialize();

		// t
		final TestBean bean = beanContainer.getBean(TestBean.class);

		assertNotNull(bean);
		assertNull(bean.innerBean);
	}

	public void testSecondLevelBeanCreation() {
		// g
		final Injector injector = new Injector();
		injector.register(TestBean.class);
		injector.register(InnerTestBean.class);

		// w
		final BeanContainer beanContainer = new BeanContainer(injector);
		beanContainer.initialize();

		// t
		final TestBean bean = beanContainer.getBean(TestBean.class);

		assertNotNull(bean);
		assertTrue(bean.innerBean instanceof InnerTestBean);
		assertEquals("test", bean.test);

	}

	public void testThirdLevelBeanCreation() {
		// g
		final Injector injector = new Injector();
		injector.register(TestBean.class);
		injector.register(InnerTestBean.class);
		injector.register(RootTestBean.class);

		// w
		final BeanContainer beanContainer = new BeanContainer(injector);
		beanContainer.initialize();

		// t
		final RootTestBean bean = beanContainer.getBean(RootTestBean.class);

		assertNotNull(bean);
		assertTrue(bean.innerBean.innerBean instanceof InnerTestBean);
		assertEquals("test", bean.innerBean.test);

	}

	static class LoopTestBean {
		@Wire private LoopTestBean child;
	}

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

	public void testSimpleInjectBean() {
		// g
		final Injector injector = new Injector();
		injector.register(InnerTestBean.class);

		// w
		final BeanContainer beanContainer = new BeanContainer(injector);
		beanContainer.initialize();

		// t
		final TestBean bean = injector.inject(new TestBean());

		assertNotNull(bean);
		assertNotNull(bean.innerBean);
	}

	public void testRegisterByName() {
		// g
		final Injector injector = new Injector();
		injector.register("InnerTestName", InnerTestBean.class);
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
		@Wire TestBean tBean;
	}

	public void testAddInitializedBean() {
		// g
		final Injector injector = new Injector();
		injector.register("InnerTestName", InnerTestBean.class);
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

	public void testReplaceBean() {
		// g
		final Injector injector = new Injector();
		injector.register("InnerTestName", TestBean.class); // A
		injector.register(RootTestBean.class);// B

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

	public void testReplaceBeanByName() {
		// g
		final Injector injector = new Injector();
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

		@Wire TestBean bean;
	}

	public void testAnnotationScopeLocalBean() {
		// g
		final Injector injector = new Injector();
		injector.register(AnnotationScopeLocalBean.class);
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

	static class TestIheritance extends TestBean {
		@Wire AnnotationScopeLocalBean bean;

		@Wire TestInterface interfacel;
	}

	private static class Test2LEvelIheritance extends TestIheritance {
	}

	interface TestInterface {

	}

	private static class TestInterfaceClass implements TestInterface {

	}

	public void testInheritanceBean() {
		// g
		final Injector injector = new Injector();
		injector.register(AnnotationScopeLocalBean.class);
		injector.register(InnerTestBean.class);
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

	public void testInheritanceSecondLevelBean() {
		// g
		final Injector injector = new Injector();
		injector.register(AnnotationScopeLocalBean.class);
		injector.register(InnerTestBean.class);
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
	
	public static class WithoutWireBean{
		@Wire TestBean testBean;
		TestBean testBeanWithoutWire;		
	}
	
	public void testShouldNotInjectWithoutWireAnnotation() {
		// g
		final Injector injector = new Injector();
		injector.register(WithoutWireBean.class);
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
	
	public static class WithWireNameBean{
		@Wire(name="test") TestBean testBean;
		@Wire TestBean test;		
	}
	public void testShouldWireByBeanByName() {
		// g
		final Injector injector = new Injector();
		injector.register(WithoutWireBean.class);
		injector.register(TestBean.class);
		injector.register("test", TestIheritance.class);		
		injector.register(TestBean.class);
		

		final BeanContainer beanContainer = new BeanContainer(injector);
		beanContainer.initialize();

		// w
		final WithoutWireBean root = beanContainer.getBean(WithoutWireBean.class);

		// t
		assertNotNull(root);
		assertTrue(root.testBean instanceof TestIheritance);
		assertFalse(root.testBeanWithoutWire instanceof TestIheritance);
	}
	
	

	// 20.12.13 - 23ms before add conqurency
	// 13.05.16 - 12ms  - szybszy sprz�t jest teraz
	//
	@Test
	public void testSpeedTime() {
		// g
		final Injector injector = new Injector();

		injector.register(AnnotationScopeLocalBean.class);
		injector.register(InnerTestBean.class);
		injector.register(TestBean.class);
		injector.register(TestIheritance.class);
		injector.register(Test2LEvelIheritance.class);
		injector.register(LoopTestBean.class);

		final BeanContainer beanContainer = new BeanContainer(injector);

		// w
		final long time = System.currentTimeMillis();
		beanContainer.initialize();
		final long time2 = System.currentTimeMillis();

		// t
		final long timeSpend = time2 - time;
		assertTrue(timeSpend < 30);

		System.err.println(timeSpend);
	}

}
