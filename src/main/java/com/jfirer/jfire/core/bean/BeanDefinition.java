package com.jfirer.jfire.core.bean;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;

import java.lang.reflect.Field;

public interface BeanDefinition
{
    void init(ApplicationContext context);

    void initEnhance();

    Object getBean();

    Field[] getAllFields(Class<?> entityClass);

    String getBeanName();

    Class<?> getType();

    void addAopManager(EnhanceManager aopManager);
}
