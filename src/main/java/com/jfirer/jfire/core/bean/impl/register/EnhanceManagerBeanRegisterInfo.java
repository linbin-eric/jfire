package com.jfirer.jfire.core.bean.impl.register;

import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.impl.definition.EnhanceMangerBeanDefinition;

public class EnhanceManagerBeanRegisterInfo extends BeanDefinitionCacheHolder
{
    private final Class<? extends EnhanceManager> ckass;

    public EnhanceManagerBeanRegisterInfo(Class<? extends EnhanceManager> ckass)
    {
        this.ckass = ckass;
    }

    @Override
    protected BeanDefinition internalGet()
    {
        return new EnhanceMangerBeanDefinition(ckass);
    }

    @Override
    public String getBeanName()
    {
        return ckass.getName();
    }

    @Override
    public Class<?> getType()
    {
        return ckass;
    }

    @Override
    public void addEnhanceManager(EnhanceManager enhanceManager)
    {
    }
}
