package cc.jfire.jfire.core.bean.impl.definition;

import cc.jfire.jfire.core.aop.EnhanceManager;
import cc.jfire.jfire.core.bean.BeanDefinition;

public class EnhanceMangerBeanDefinition implements BeanDefinition
{
    private final Class<? extends EnhanceManager> type;
    private final EnhanceManager                  enhanceManager;

    public EnhanceMangerBeanDefinition(Class<? extends EnhanceManager> type)
    {
        if (!EnhanceManager.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException();
        }
        this.type = type;
        try
        {
            enhanceManager = type.getDeclaredConstructor().newInstance();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getBean()
    {
        return enhanceManager;
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
