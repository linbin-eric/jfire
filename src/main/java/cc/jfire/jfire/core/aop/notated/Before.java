package cc.jfire.jfire.core.aop.notated;

import cc.jfire.jfire.core.aop.notated.support.MatchTargetMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 前置增强注解，标注的方法会在目标方法执行前执行
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Before
{
    /**
     * 匹配目标方法的表达式
     *
     * @return 匹配表达式
     */
    String value() default "";

    /**
     * 自定义匹配器类
     *
     * @return 匹配器类
     */
    Class<? extends MatchTargetMethod> custom() default MatchTargetMethod.class;
}
