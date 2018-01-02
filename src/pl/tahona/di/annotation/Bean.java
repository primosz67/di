package pl.tahona.di.annotation;

import pl.tahona.di.BeanScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    String value() default "";
    BeanScope scope() default BeanScope.SINGLETON;
}


