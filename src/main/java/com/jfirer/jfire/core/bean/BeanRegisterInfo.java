package com.jfirer.jfire.core.bean;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.bean.BeanDefinition;

import java.lang.reflect.Field;

public interface BeanRegisterInfo
{
    BeanDefinition get();

    void init(ApplicationContext context);

    void initEnhance();


    Field[] getAllFields(Class<?> entityClass);

    String getBeanName();

    Class<?> getType();

    void addAopManager(EnhanceManager aopManager);
}
