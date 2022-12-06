package com.jfirer.jfire.core.aop.impl.transaction;

import static com.jfirer.jfire.core.aop.impl.transaction.Propagation.*;

public abstract class JdbcTransactionManager implements TransactionManager
{

    public static ThreadLocal<JdbcTransactionState> CONTEXT = new ThreadLocal<>();

    @Override
    public TransactionState beginTransAction(Propagation propagation)
    {
        switch (propagation)
        {
            case REQUIRED:
            {
                JdbcTransactionState prevState = CONTEXT.get();
                if (prevState == null)
                {
                    ConnectionHolder connectionHolder = openConnection();
                    connectionHolder.beginTransaction();
                    JdbcTransactionState newState = new JdbcTransactionState(REQUIRED, true, true, true, prevState, connectionHolder);
                    CONTEXT.set(newState);
                    return newState;
                }
                else if (prevState.isInDatabaseTransaction())
                {
                    JdbcTransactionState currentState = new JdbcTransactionState(REQUIRED, false, false, true, prevState, prevState.getConnectionHolder());
                    CONTEXT.set(currentState);
                    return currentState;
                }
                else
                {
                    prevState.getConnectionHolder().beginTransaction();
                    JdbcTransactionState currentState = new JdbcTransactionState(REQUIRED, false, true, true, prevState, prevState.getConnectionHolder());
                    CONTEXT.set(currentState);
                    return currentState;
                }
            }
            case SUPPORTS:
            {
                JdbcTransactionState prevState = CONTEXT.get();
                if (prevState == null)
                {
                    ConnectionHolder     connectionHolder = openConnection();
                    JdbcTransactionState newState         = new JdbcTransactionState(SUPPORTS, true, false, false, prevState, connectionHolder);
                    CONTEXT.set(newState);
                    return newState;
                }
                else
                {
                    JdbcTransactionState newState = new JdbcTransactionState(SUPPORTS, false, false, prevState.isInDatabaseTransaction(), prevState, prevState.getConnectionHolder());
                    CONTEXT.set(newState);
                    return newState;
                }
            }
            case MANDATORY:
            {
                JdbcTransactionState prevState = CONTEXT.get();
                if (prevState == null)
                {
                    throw new IllegalStateException("当前传播级别为MANDATORY，但是上下文中没有已经存在的事务");
                }
                else
                {
                    JdbcTransactionState newState = new JdbcTransactionState(MANDATORY, false, false, prevState.isInDatabaseTransaction(), prevState, prevState.getConnectionHolder());
                    CONTEXT.set(newState);
                    return newState;
                }
            }
            case REQUIRES_NEW:
                JdbcTransactionState prevState = CONTEXT.get();
                ConnectionHolder newConnection = openConnection();
                newConnection.beginTransaction();
                JdbcTransactionState newState = new JdbcTransactionState(REQUIRES_NEW, true, true, true, prevState, newConnection);
                CONTEXT.set(newState);
                return newState;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * 开启一个新的链接
     *
     * @return
     */
    protected abstract ConnectionHolder openConnection();

    @Override
    public void commit(TransactionState state)
    {
        if (((JdbcTransactionState) state).isRootTransaction())
        {
            ((JdbcTransactionState) state).getConnectionHolder().commit();
            CONTEXT.set(((JdbcTransactionState) state).getPrev());
        }
        if (((JdbcTransactionState) state).isRootConnection())
        {
            closeCurrentConnection(state);
        }
    }

    @Override
    public void rollback(TransactionState state, Throwable e)
    {
        if (((JdbcTransactionState) state).isRootTransaction())
        {
            ((JdbcTransactionState) state).getConnectionHolder().rollback();
            CONTEXT.set(((JdbcTransactionState) state).getPrev());
        }
        if (((JdbcTransactionState) state).isRootConnection())
        {
            closeCurrentConnection(state);
        }
    }

    private void closeCurrentConnection(TransactionState state)
    {
        ConnectionHolder connectionHolder = ((JdbcTransactionState) state).getConnectionHolder();
        connectionHolder.close();
    }
}
