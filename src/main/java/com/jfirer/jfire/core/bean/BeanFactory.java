package com.jfirer.jfire.core.bean;

import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;

public interface BeanFactory
{
    <E> E getInstance(InstanceDescriptor beanDescriptor);
    /**
     * 返回该Bean定义的原始对象，也即没有经过增强、初始化等一系列操作的原始对象。
     * @param beanDefinition
     * @return
     * @param <E>
     */
//    <E> E getUnModifyInstance(BeanDefinition beanDefinition);
}