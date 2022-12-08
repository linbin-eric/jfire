package com.jfirer.jfire.core.beanfactory.impl;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;

public class SelectedBeanFactory implements BeanFactory
{
    private final ApplicationContext           applicationContext;
    private final String                       beanFactoryBeanName;
    private final Class<? extends BeanFactory> beanFactoryClass;

    public SelectedBeanFactory(ApplicationContext applicationContext, String beanFactoryBeanName, Class<? extends BeanFactory> beanFactoryClass)
    {
        this.applicationContext = applicationContext;
        this.beanFactoryBeanName = beanFactoryBeanName;
        this.beanFactoryClass = beanFactoryClass;
    }

    @Override
    public <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition)
    {
        if (beanFactoryBeanName == null)
        {
            return applicationContext.getBean(beanFactoryClass).getUnEnhanceyInstance(beanDefinition);
        }
        else
        {
            return ((BeanFactory) applicationContext.getBean(beanFactoryBeanName)).getUnEnhanceyInstance(beanDefinition);
        }
    }
}
