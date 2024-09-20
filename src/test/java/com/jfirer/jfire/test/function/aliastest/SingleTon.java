package com.jfirer.jfire.test.function.aliastest;

import com.jfirer.baseutil.Resource;
import com.jfirer.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Resource(shareable = true)
public @interface SingleTon
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String value() default "";
}
