package com.jfirer.jfire.core.bean.impl.definition;

import com.jfirer.jfire.core.bean.BeanDefinition;

public class OutterBeanDefinition implements BeanDefinition
{
    private final String beanName;
    private final Object outter;

    public OutterBeanDefinition(String beanName, Object outter)
    {
        this.beanName = beanName;
        this.outter = outter;
    }

    @Override
    public Object getBean()
    {
        return outter;
    }

    @Override
    public String getBeanName()
    {
        return beanName;
    }

    @Override
    public Class<?> getType()
    {
        return outter.getClass();
    }
}
