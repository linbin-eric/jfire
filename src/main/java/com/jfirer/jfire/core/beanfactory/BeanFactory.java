package com.jfirer.jfire.core.beanfactory;

import com.jfirer.jfire.core.bean.BeanDefinition;

public interface BeanFactory
{
    /**
     * 返回该Bean定义的原始对象，也即没有经过增强、初始化等一系列操作的原始对象。
     *
     * @param beanDefinition
     * @param <E>
     * @return
     */
    <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition);
}