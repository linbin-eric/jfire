package com.jfirer.jfire.core.aop.notated;

import com.jfirer.jfire.core.aop.impl.support.transaction.Propagation;

import java.lang.annotation.*;

import static com.jfirer.jfire.core.aop.impl.support.transaction.Propagation.REQUIRED;

/**
 * 使用该注解表明该类的公共方法或者注解方法是一个事务方法
 *
 * @author linbin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Transactional
{
    Propagation propagation() default REQUIRED;
}
