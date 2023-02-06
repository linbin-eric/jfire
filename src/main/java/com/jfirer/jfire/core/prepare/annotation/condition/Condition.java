package com.jfirer.jfire.core.prepare.annotation.condition;

import com.jfirer.jfire.core.ApplicationContext;

import java.lang.reflect.AnnotatedElement;

public interface Condition
{
    boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage);
}
