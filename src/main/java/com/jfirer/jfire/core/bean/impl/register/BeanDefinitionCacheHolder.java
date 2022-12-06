package com.jfirer.jfire.core.bean.impl.register;

import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;

/**
 * 一个BeanRegisterInfo产生的BeanDefinition实例必须是唯一的，因此通过这个父类来保证唯一。
 */
public abstract class BeanDefinitionCacheHolder implements BeanRegisterInfo
{
    private volatile BeanDefinition cached;

    @Override
    public final BeanDefinition get()
    {
        if (cached != null)
        {
            return cached;
        }
        synchronized (this)
        {
            if (cached != null)
            {
                return cached;
            }
            else
            {
                cached = internalGet();
                return cached;
            }
        }
    }

    protected abstract BeanDefinition internalGet();
}
