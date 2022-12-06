package com.jfirer.jfire.core;

import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;

import java.util.Collection;

public interface ApplicationContext
{
    <E> E getBean(Class<E> ckass);

    <E> Collection<E> getBeans(Class<E> ckass);

    <E> E getBean(String beanName);

    /**
     * 刷新上下文，动作包含：<br/>
     * 1. 清空容器内的所有Bean实例。<br/>
     * 2. 注册默认的Bean信息到容器。<br/>
     * 2. 读取配置类信息，如果配置类注解了Import注解，则导入对应的类。并且将配置类注册为一个Bean。<br/>
     * 3. 执行当前的预处理器处理链<br/>
     */
    void refresh();

    ////
    Environment getEnv();

    Collection<BeanRegisterInfo> getAllBeanRegisterInfos();

    BeanRegisterInfo getBeanRegisterInfo(Class<?> ckass);

    BeanRegisterInfo getBeanRegisterInfo(String beanName);

    Collection<BeanRegisterInfo> getBeanRegisterInfos(Class<?> ckass);

    /**
     * 注册一个类，框架会分析导入类的类型：<br/>
     * 1. 如果实现了JfirePrepare接口，则调用registerJfirePrepare接口进行注册。<br/>
     * 2. 如果类上标记为了Configuration注解，则调用registerConfiguration接口进行注册。<br/>
     * 3. 如果不是抽象类或者接口，则注册为一个Bean。<br/>
     * <br/>
     * .
     *
     * @param ckass
     */
    RegisterResult register(Class<?> ckass);


    /**
     * 注册Bean定义，注册成功返回true。如果Bean名称相同，则不再注册，直接返回false。
     */
    RegisterResult registerBeanRegisterInfo(BeanRegisterInfo beanRegisterInfo);


    enum NeedRefresh
    {
        YES,
        NO
    }

    enum RegisterResult
    {
        PREPARE,
        CONFIGURATION,
        BEAN,
        NODATA
    }
}
