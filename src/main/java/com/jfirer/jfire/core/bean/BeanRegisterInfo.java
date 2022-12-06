package com.jfirer.jfire.core.bean;

import com.jfirer.jfire.core.aop.EnhanceManager;

public interface BeanRegisterInfo
{
    BeanDefinition get();

    String getBeanName();

    Class<?> getType();

    void addEnhanceManager(EnhanceManager enhanceManager);
}
