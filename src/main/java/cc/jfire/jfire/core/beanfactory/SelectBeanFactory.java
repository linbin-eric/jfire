package cc.jfire.jfire.core.beanfactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SelectBeanFactory
{
    String value() default "";

    Class<? extends BeanFactory> beanFactoryType() default BeanFactory.class;
}
