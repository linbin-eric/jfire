package cc.jfire.jfire.core.bean.impl.register;

import cc.jfire.jfire.core.aop.EnhanceManager;
import cc.jfire.jfire.core.bean.BeanDefinition;
import cc.jfire.jfire.core.bean.impl.definition.ContextPrepareBeanDefinition;
import cc.jfire.jfire.core.prepare.ContextPrepare;

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
