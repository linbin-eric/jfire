package com.jfirer.jfire.core.beanfactory.impl;

import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;

public class ClassBeanFactory implements BeanFactory
{
    public static final ClassBeanFactory INSTANCE = new ClassBeanFactory();

    @Override
    public <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition)
    {
        try
        {
            return (E) beanDefinition.getType().getDeclaredConstructor().newInstance();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
}
