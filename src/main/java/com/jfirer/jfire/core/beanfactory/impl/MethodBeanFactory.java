package com.jfirer.jfire.core.beanfactory.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;

import java.lang.reflect.Method;

public class MethodBeanFactory implements BeanFactory
{
    private final ApplicationContext applicationContext;
    private final Method             method;

    public MethodBeanFactory(ApplicationContext applicationContext, Method method)
    {
        this.applicationContext = applicationContext;
        this.method = method;
    }

    @Override
    public <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition)
    {
        Object hostBean = applicationContext.getBean(method.getDeclaringClass());
        if (hostBean == null)
        {
            throw new BeanDefinitionCanNotFindException(method.getDeclaringClass());
        }
        method.setAccessible(true);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[]   params         = new Object[parameterTypes.length];
        try
        {
            if (params.length == 0)
            {
                return (E) method.invoke(hostBean, null);
            }
            else
            {
                for (int i = 0; i < parameterTypes.length; i++)
                {
                    params[i] = applicationContext.getBean(parameterTypes[i]);
                }
                return (E) method.invoke(hostBean, params);
            }
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
}
