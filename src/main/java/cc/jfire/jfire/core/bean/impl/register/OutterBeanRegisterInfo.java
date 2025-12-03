package cc.jfire.jfire.core.bean.impl.register;

import cc.jfire.jfire.core.aop.EnhanceManager;
import cc.jfire.jfire.core.bean.BeanDefinition;
import cc.jfire.jfire.core.bean.impl.definition.OutterBeanDefinition;

public class OutterBeanRegisterInfo extends BeanDefinitionCacheHolder
{
    private final Object outter;
    private final String beanName;

    public OutterBeanRegisterInfo(Object outter, String beanName)
    {
        this.outter = outter;
        this.beanName = beanName;
    }

    @Override
    protected BeanDefinition internalGet()
    {
        return new OutterBeanDefinition(beanName, outter);
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

    @Override
    public void addEnhanceManager(EnhanceManager enhanceManager)
    {
    }
}
