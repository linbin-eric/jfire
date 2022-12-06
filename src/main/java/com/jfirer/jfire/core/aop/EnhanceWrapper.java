package com.jfirer.jfire.core.aop;

import com.jfirer.jfire.core.ApplicationContext;

public interface EnhanceWrapper
{
    /**
     * 设置被代理的实例
     *
     * @param instance
     */
    void setHost(Object instance);

    void setEnhanceFields(ApplicationContext context);
}
