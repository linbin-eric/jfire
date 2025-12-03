package cc.jfire.jfire.core.prepare.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean注解，用于在Configuration类中声明Bean
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean
{
    /**
     * bean的名称，如果不填写的话，默认为方法名
     *
     * @return Bean名称
     */
    String name() default "";

    /**
     * 是否为原型模式，默认为单例
     *
     * @return true表示原型模式，false表示单例模式
     */
    boolean prototype() default false;
}
