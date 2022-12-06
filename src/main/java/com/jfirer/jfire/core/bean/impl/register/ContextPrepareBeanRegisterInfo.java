package com.jfirer.jfire.core.bean.impl.register;

import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.impl.definition.ContextPrepareBeanDefinition;
import com.jfirer.jfire.core.prepare.ContextPrepare;

public class ContextPrepareBeanRegisterInfo extends BeanDefinitionCacheHolder
{
    private final Class<? extends ContextPrepare> ckass;

    public ContextPrepareBeanRegisterInfo(Class<? extends ContextPrepare> ckass) {this.ckass = ckass;}


    @Override
    protected BeanDefinition internalGet()
    {
        return new ContextPrepareBeanDefinition(ckass);
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
