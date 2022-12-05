package com.jfirer.jfire.core.aop.impl.transaction;

public class JdbcTransactionState implements TransactionState
{
    private final Propagation          propagation;
    //该事务对象，是否伴随着一个新的链接。
    private final boolean              rootConnection;
    //该事务状态是否是根事务。根事务代表该事务对象是物理上开启的事务，才能进行提交、回滚等操作。
    private final boolean              rootTransaction;
    /**
     * 该状态对象，本身是否处于数据库事务中。
     */
    private final boolean              inDatabaseTransaction;
    private final JdbcTransactionState prev;
    private final ConnectionHolder     connectionHolder;

    public JdbcTransactionState(Propagation propagation, boolean rootConnection, boolean rootTransaction, boolean inDatabaseTransaction, JdbcTransactionState prev, ConnectionHolder connectionHolder)
    {
        this.propagation = propagation;
        this.rootConnection = rootConnection;
        this.rootTransaction = rootTransaction;
        this.inDatabaseTransaction = inDatabaseTransaction;
        this.prev = prev;
        this.connectionHolder = connectionHolder;
    }

    @Override
    public Propagation propagation()
    {
        return propagation;
    }

    public Propagation getPropagation()
    {
        return propagation;
    }

    public boolean isRootConnection()
    {
        return rootConnection;
    }

    public boolean isRootTransaction()
    {
        return rootTransaction;
    }

    public boolean isInDatabaseTransaction()
    {
        return inDatabaseTransaction;
    }

    public JdbcTransactionState getPrev()
    {
        return prev;
    }

    public ConnectionHolder getConnectionHolder()
    {
        return connectionHolder;
    }
}
