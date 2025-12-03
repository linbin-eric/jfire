package cc.jfire.jfire.core.bean.impl.definition;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.beanfactory.BeanFactory;
import cc.jfire.jfire.core.inject.InjectHandler;

import java.lang.reflect.Method;

public class SingletonBeanDefinition extends PrototypeBeanDefinition
{

    private volatile Object cachedSingletonInstance;

    public SingletonBeanDefinition(BeanFactory beanFactory, ApplicationContext context, Method postConstructMethod, InjectHandler[] injectHandlers, Class<?> enhanceType, Class type, String beanName)
    {
        super(beanFactory, context, postConstructMethod, injectHandlers, enhanceType, type, beanName);
    }

    @Override
    public Object getBean()
    {
        if (cachedSingletonInstance != null)
        {
            return cachedSingletonInstance;
        }
        else
        {
            synchronized (this)
            {
                if (cachedSingletonInstance != null)
                {
                    return cachedSingletonInstance;
                }
                cachedSingletonInstance = buildInstance();
                return cachedSingletonInstance;
            }
        }
    }
}
