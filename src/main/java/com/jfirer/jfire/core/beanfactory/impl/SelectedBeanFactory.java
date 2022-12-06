package com.jfirer.jfire.core.beanfactory.impl;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;

public class SelectedBeanFactory implements BeanFactory
{
    private final ApplicationContext applicationContext;
    private final String beanFactoryBeanName;

    public SelectedBeanFactory(ApplicationContext applicationContext, String beanFactoryBeanName)
    {
        this.applicationContext = applicationContext;
        this.beanFactoryBeanName = beanFactoryBeanName;
    }

    @Override
    public <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition)
    {
        BeanFactory beanFactory = (BeanFactory) applicationContext.getBean(beanFactoryBeanName);
        return beanFactory.getUnEnhanceyInstance(beanDefinition);
    }
}
