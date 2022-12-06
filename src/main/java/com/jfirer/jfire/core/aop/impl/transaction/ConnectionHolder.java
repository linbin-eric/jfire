package com.jfirer.jfire.core.aop.impl.transaction;

public interface ConnectionHolder
{

    void beginTransaction();

    void commit();

    void rollback();

    void close();

    boolean isClosed();
}
