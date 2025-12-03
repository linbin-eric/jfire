package cc.jfire.jfire.core.aop.notated.support;

import java.lang.reflect.Method;

/**
 * 目标方法匹配器接口，用于判断方法是否需要被增强
 */
public interface MatchTargetMethod
{
    /**
     * 判断方法是否匹配
     *
     * @param method 待匹配的方法
     * @return true表示匹配，false表示不匹配
     */
    boolean match(Method method);
}
