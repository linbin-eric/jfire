package cc.jfire.jfire.core.prepare.annotation;

import cc.jfire.jfire.core.prepare.processor.AddPropertyProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于添加属性配置的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AddPropertyProcessor.class)
public @interface AddProperty
{
    /**
     * 属性键值对数组，格式为 "key=value"
     *
     * @return 属性键值对数组
     */
    String[] value();
}
