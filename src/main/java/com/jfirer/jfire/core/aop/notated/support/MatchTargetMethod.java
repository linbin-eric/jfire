package com.jfirer.jfire.core.aop.notated.support;

import java.lang.reflect.Method;

public interface MatchTargetMethod
{
    boolean match(Method method);
}
