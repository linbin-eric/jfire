package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.jfire.core.prepare.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(MyBeanImport.class)
public @interface MyImport
{
    String name();
}
