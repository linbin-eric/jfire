package cc.jfire.jfire.core.prepare.annotation.condition;

import cc.jfire.jfire.core.ApplicationContext;

import java.lang.reflect.AnnotatedElement;

public interface Condition
{
    boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage);
}
