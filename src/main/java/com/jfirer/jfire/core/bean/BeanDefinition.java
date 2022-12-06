package com.jfirer.jfire.core.bean;

public interface BeanDefinition
{

    Object getBean();

    String getBeanName();

    Class<?> getType();
}
