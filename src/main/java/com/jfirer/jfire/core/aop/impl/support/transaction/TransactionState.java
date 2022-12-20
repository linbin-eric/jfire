package com.jfirer.jfire.core.aop.impl.support.transaction;

public interface TransactionState
{

    /**
     * 返回该事务状态的传播级别
     *
     * @return
     */
    Propagation propagation();
}
