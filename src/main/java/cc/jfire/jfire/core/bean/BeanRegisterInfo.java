package cc.jfire.jfire.core.bean;

import cc.jfire.jfire.core.aop.EnhanceManager;

/**
 * Bean注册信息接口，用于管理Bean的注册和增强
 */
public interface BeanRegisterInfo
{
    /**
     * 获取Bean定义
     *
     * @return Bean定义实例
     */
    BeanDefinition get();

    /**
     * 获取Bean名称
     *
     * @return Bean名称
     */
    String getBeanName();

    /**
     * 获取Bean类型
     *
     * @return Bean的Class类型
     */
    Class<?> getType();

    /**
     * 添加增强管理器
     *
     * @param enhanceManager 增强管理器
     */
    void addEnhanceManager(EnhanceManager enhanceManager);
}
