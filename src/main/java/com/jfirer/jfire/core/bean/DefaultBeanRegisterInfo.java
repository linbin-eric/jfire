package com.jfirer.jfire.core.bean;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceCallbackForBeanInstance;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.inject.InjectHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class DefaultBeanRegisterInfo implements BeanRegisterInfo
{
    private          boolean                          prototype;
    // 如果是单例的情况，后续只会使用该实例
    private volatile Object                           cachedSingletonInstance;
    /******/
    // 该Bean的类
    private          Class<?>                         type;
    // 增强后的类，如果没有增强标记，该属性为空
    private          Class<?>                         enhanceType;
    private          Set<EnhanceManager>              aopManagers = new HashSet<EnhanceManager>();
    private          EnhanceCallbackForBeanInstance[] enhanceCallbackForBeanInstances;
    private          String                           beanName;
    // 标注@PostConstruct的方法
    private          Method                           postConstructMethod;
    private InjectHandler[]    injectHandlers;
    private ApplicationContext context;

    @Override
    public BeanDefinition get()
    {
        return null;
    }

    @Override
    public void init(ApplicationContext context)
    {
    }

    @Override
    public void initEnhance()
    {
    }

    @Override
    public Field[] getAllFields(Class<?> entityClass)
    {
        return new Field[0];
    }

    @Override
    public String getBeanName()
    {
        return null;
    }

    @Override
    public Class<?> getType()
    {
        return null;
    }

    @Override
    public void addAopManager(EnhanceManager aopManager)
    {
    }
}
