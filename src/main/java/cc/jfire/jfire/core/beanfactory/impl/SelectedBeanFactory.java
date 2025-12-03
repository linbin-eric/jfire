package cc.jfire.jfire.core.beanfactory.impl;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.bean.BeanDefinition;
import cc.jfire.jfire.core.beanfactory.BeanFactory;

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
