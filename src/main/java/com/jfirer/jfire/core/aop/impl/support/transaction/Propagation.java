package com.jfirer.jfire.core.aop.impl.support.transaction;

public enum Propagation
{

    /**
     * 支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。
     */
    REQUIRED(1),
    /**
     * 支持当前事务，如果当前没有事务，就以非事务方式执行。
     */
    SUPPORTS(2),
    /**
     * 支持当前事务，如果当前没有事务，就抛出异常
     */
    MANDATORY(3),
    /*
     * 新建事务，如果当前存在事务，把当前事务挂起
     */
    REQUIRES_NEW(4);

    int code;

    Propagation(int code)
    {
        this.code = code;
    }

    public int code()
    {
        return code;
    }
}

