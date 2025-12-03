package cc.jfire.jfire.test.function.aliastest;


import cc.jfire.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@InitMethod(name = "")
public @interface MyMethod
{
    @OverridesAttribute(annotation = InitMethod.class, name = "name") String load();
}
