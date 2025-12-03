package cc.jfire.jfire.core.beanfactory;

import cc.jfire.jfire.core.bean.BeanDefinition;

/**
 * Bean工厂接口，用于创建Bean实例
 */
public interface BeanFactory
{
    /**
     * 返回该Bean定义的原始对象，也即没有经过增强、初始化等一系列操作的原始对象。
     *
     * @param beanDefinition Bean定义
     * @param <E> Bean类型
     * @return 未增强的原始Bean实例
     */
    <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition);
}