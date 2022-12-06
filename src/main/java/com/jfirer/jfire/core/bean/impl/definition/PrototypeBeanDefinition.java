package com.jfirer.jfire.core.bean.impl.definition;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.EnhanceWrapper;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;
import com.jfirer.jfire.core.inject.InjectHandler;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.exception.NewBeanInstanceException;
import com.jfirer.jfire.exception.PostConstructMethodException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PrototypeBeanDefinition implements BeanDefinition
{
    protected static final ThreadLocal<Map<String, Object>> tmpBeanInstanceMap = ThreadLocal.withInitial(() -> new HashMap<String, Object>());
    protected              BeanFactory                   beanFactory;
    protected              ApplicationContext               context;
    // 标注@PostConstruct的方法
    protected              Method                           postConstructMethod;
    protected              InjectHandler[]                  injectHandlers;
    protected              Class<?>                         enhanceType;
    protected              Class                            type;
    protected              String                           beanName;

    public PrototypeBeanDefinition(BeanFactory beanFactory, ApplicationContext context, Method postConstructMethod, InjectHandler[] injectHandlers, Class<?> enhanceType, Class type, String beanName)
    {
        if (ContextPrepare.class.isAssignableFrom(type) ||EnhanceManager.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("框架代码自身错误，ContextPrepare 或 EnhanceManager 类型的Bean应该要选择特定的BeanDefinition");
        }
        this.beanFactory = beanFactory;
        this.context = context;
        this.postConstructMethod = postConstructMethod;
        this.injectHandlers = injectHandlers;
        this.enhanceType = enhanceType;
        this.type = type;
        this.beanName = beanName;
    }

    protected synchronized Object buildInstance()
    {
        Map<String, Object> map = tmpBeanInstanceMap.get();
        boolean cleanMark = map.isEmpty();
        Object instance = map.get(getBeanName());
        if (instance != null)
        {
            return instance;
        }
        Object unEnhanceInstance;
        unEnhanceInstance = instance = beanFactory.getUnEnhanceyInstance(this);
        if (enhanceType != null)
        {
            try
            {
                EnhanceWrapper newInstance = (EnhanceWrapper) enhanceType.getDeclaredConstructor()
                                                                         .newInstance();
                newInstance.setHost(unEnhanceInstance);
                newInstance.setEnhanceFields(context);
                instance = newInstance;
            }
            catch (Throwable e)
            {
                throw new NewBeanInstanceException(e);
            }
        }
        map.put(getBeanName(), instance);
        if (injectHandlers.length != 0)
        {
            for (InjectHandler each : injectHandlers)
            {
                each.inject(unEnhanceInstance);
            }
        }
        if (postConstructMethod != null)
        {
            try
            {
                postConstructMethod.invoke(instance);
            }
            catch (Exception e)
            {
                throw new PostConstructMethodException(e);
            }
        }
        if (cleanMark)
        {
            map.clear();
        }
        return instance;
    }

    @Override
    public Object getBean()
    {
        return buildInstance();
    }

    @Override
    public String getBeanName()
    {
        return beanName;
    }

    @Override
    public Class<?> getType()
    {
        return type;
    }
}
