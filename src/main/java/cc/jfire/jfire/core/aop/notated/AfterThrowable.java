package cc.jfire.jfire.core.aop.notated;

import cc.jfire.jfire.core.aop.notated.support.MatchTargetMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常增强注解，标注的方法会在目标方法抛出异常后执行
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterThrowable
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
