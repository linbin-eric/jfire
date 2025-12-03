package cc.jfire.jfire.core;

import cc.jfire.jfire.core.bean.BeanRegisterInfo;
import cc.jfire.jfire.util.YmlConfig;

import java.util.Collection;

/**
 * 应用上下文接口，提供Bean的获取、注册等核心功能
 */
public interface ApplicationContext
{
    /**
     * 根据类型获取Bean实例
     *
     * @param ckass Bean类型
     * @param <E> Bean类型参数
     * @return Bean实例
     */
    <E> E getBean(Class<E> ckass);

    /**
     * 根据类型获取所有匹配的Bean实例集合
     *
     * @param ckass Bean类型
     * @param <E> Bean类型参数
     * @return Bean实例集合
     */
    <E> Collection<E> getBeans(Class<E> ckass);

    /**
     * 根据名称获取Bean实例
     *
     * @param beanName Bean名称
     * @param <E> Bean类型参数
     * @return Bean实例
     */
    <E> E getBean(String beanName);

    /**
     * 获取配置信息
     *
     * @return Yml配置对象
     */
    YmlConfig getConfig();

    /**
     * 获取所有Bean注册信息
     *
     * @return Bean注册信息集合
     */
    Collection<BeanRegisterInfo> getAllBeanRegisterInfos();

    /**
     * 根据类型获取Bean注册信息
     *
     * @param ckass Bean类型
     * @return Bean注册信息
     */
    BeanRegisterInfo getBeanRegisterInfo(Class<?> ckass);

    /**
     * 根据名称获取Bean注册信息
     *
     * @param beanName Bean名称
     * @return Bean注册信息
     */
    BeanRegisterInfo getBeanRegisterInfo(String beanName);

    /**
     * 根据类型获取所有匹配的Bean注册信息集合
     *
     * @param ckass Bean类型
     * @return Bean注册信息集合
     */
    Collection<BeanRegisterInfo> getBeanRegisterInfos(Class<?> ckass);

    /**
     * 使容器可用，完成初始化
     */
    void makeAvailable();

    /**
     * 注册一个类，框架会分析导入类的类型：<br/>
     * 1. 如果实现了JfirePrepare接口，则调用registerJfirePrepare接口进行注册。<br/>
     * 2. 如果类上标记为了Configuration注解，则调用registerConfiguration接口进行注册。<br/>
     * 3. 如果不是抽象类或者接口，则注册为一个Bean。<br/>
     *
     * @param ckass 要注册的类
     * @return 注册结果
     */
    RegisterResult register(Class<?> ckass);

    /**
     * 注册Bean定义，注册成功返回true。如果Bean名称相同，则不再注册，直接返回false。
     *
     * @param beanRegisterInfo Bean注册信息
     * @return 注册结果
     */
    RegisterResult registerBeanRegisterInfo(BeanRegisterInfo beanRegisterInfo);

    /**
     * 是否发现新的ContextPrepare枚举
     */
    enum FoundNewContextPrepare
    {
        /** 是 */
        YES,
        /** 否 */
        NO
    }

    /**
     * 注册结果枚举
     */
    enum RegisterResult
    {
        /** 预处理器 */
        PREPARE,
        /** 配置类 */
        CONFIGURATION,
        /** Bean */
        BEAN,
        /** 无数据 */
        NODATA
    }

    /**
     * 启动应用上下文
     *
     * @param bootClass 启动类
     * @return 应用上下文实例
     */
    static ApplicationContext boot(Class<?> bootClass)
    {
        return new DefaultApplicationContext(bootClass);
    }

    /**
     * 启动应用上下文（无启动类）
     *
     * @return 应用上下文实例
     */
    static ApplicationContext boot()
    {
        return new DefaultApplicationContext();
    }
}
