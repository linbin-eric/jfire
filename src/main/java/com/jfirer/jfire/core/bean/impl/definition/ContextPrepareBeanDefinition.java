package com.jfirer.jfire.core.bean.impl.definition;

import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.prepare.ContextPrepare;

public class ContextPrepareBeanDefinition implements BeanDefinition
{
    private final Class<? extends ContextPrepare> type;
    private final ContextPrepare                  contextPrepare;

    public ContextPrepareBeanDefinition(Class<? extends ContextPrepare> type)
    {
        if (ContextPrepare.class.isAssignableFrom(type)==false)
        {
            throw new IllegalArgumentException();
        }
        this.type = type;
        try
        {
            contextPrepare = type.getDeclaredConstructor().newInstance();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getBean()
    {
        return contextPrepare;
    }

    @Override
    public String getBeanName()
    {
        return type.getName();
    }

    @Override
    public Class<?> getType()
    {
        return type;
    }
}
