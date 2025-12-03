package cc.jfire.jfire.core.prepare.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当一个接口有多个实现的时候，如果进行依赖注入，则选择有该注解的实现
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary
{}
