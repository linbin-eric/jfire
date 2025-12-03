package cc.jfire.jfire.test.function.aliastest;


import cc.jfire.baseutil.Resource;
import cc.jfire.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface TestAlias
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String test() default "test";
}
