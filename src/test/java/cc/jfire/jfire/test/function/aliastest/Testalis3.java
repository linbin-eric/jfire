package cc.jfire.jfire.test.function.aliastest;


import cc.jfire.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@TestAlias
public @interface Testalis3
{
    @OverridesAttribute(annotation = TestAlias.class, name = "test") String t();
}
