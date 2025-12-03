package cc.jfire.jfire.test.function.aliastest;


import cc.jfire.baseutil.Resource;
import cc.jfire.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Resource(shareable = true)
public @interface SingleTon
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String value() default "";
}
