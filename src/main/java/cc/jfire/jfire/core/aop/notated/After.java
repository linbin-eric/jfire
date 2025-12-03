package cc.jfire.jfire.core.aop.notated;

import cc.jfire.jfire.core.aop.notated.support.MatchTargetMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 后置增强注解，标注的方法会在目标方法执行后执行
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface After
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
