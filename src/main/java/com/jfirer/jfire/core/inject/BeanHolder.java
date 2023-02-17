package com.jfirer.jfire.core.inject;

public interface BeanHolder<T>
{
    /**
     * 返回当前Bean对象
     * @return
     */
    T getSelf();
}
