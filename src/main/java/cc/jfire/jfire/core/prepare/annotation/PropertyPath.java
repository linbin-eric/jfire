package cc.jfire.jfire.core.prepare.annotation;

import cc.jfire.jfire.core.prepare.processor.PropertyPathProcessor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(PropertyPathProcessor.class)
public @interface PropertyPath
{
    String[] value();
}
