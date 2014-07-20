package com.tahona.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Bean annotation to add configuration for class;
 * @author primosz67
 *
 */
@Target(value=ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean{
	BeanScope scope() default BeanScope.SINGLETON;
}


