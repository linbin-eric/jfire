package com.jfirer.jfire.core.aop.impl.support.transaction;

import lombok.Data;
import lombok.SneakyThrows;

import javax.sql.DataSource;

@Data
public class JdbcTransactionManager implements TransactionManager
{
    protected final     DataSource                        dataSource;
    public static final ThreadLocal<JdbcTransactionState> CONTEXT = new ThreadLocal<>();

    @SneakyThrows
    @Override
    public TransactionState beginTransAction(Propagation propagation)
    {
        switch (propagation)
        {
            case REQUIRED:
            {
                JdbcTransactionState currentState = CONTEXT.get();
                JdbcTransactionState state;
                if (currentState == null)
                {
                    state = new JdbcTransactionState(null, propagation, dataSource.getConnection());
                    state.setAutoCommit(false);
                    CONTEXT.set(state);
                }
                else if (currentState.findConnection().getAutoCommit())
                {
                    state = new JdbcTransactionState(currentState, propagation, null);
                    state.setAutoCommit(false);
                    CONTEXT.set(state);
                }
                else
                {
                    state = new JdbcTransactionState(currentState, propagation, null);
                    CONTEXT.set(state);
                }
                return state;
            }
            case SUPPORTS:
            {
                JdbcTransactionState currentState = CONTEXT.get();
                JdbcTransactionState state;
                if (currentState == null)
                {
                    state = new JdbcTransactionState(null, propagation, dataSource.getConnection());
                }
                else
                {
                    state = new JdbcTransactionState(currentState, propagation, null);
                }
                CONTEXT.set(state);
                return state;
            }
            case MANDATORY:
            {
                JdbcTransactionState currentState = CONTEXT.get();
                if (currentState == null || currentState.findConnection().getAutoCommit())
                {
                    throw new IllegalStateException("当前传播级别为MANDATORY，但是上下文中没有已经存在的事务");
                }
                else
                {
                    JdbcTransactionState newState = new JdbcTransactionState(currentState, propagation, null);
                    CONTEXT.set(newState);
                    return newState;
                }
            }
            case REQUIRES_NEW:
            {
                JdbcTransactionState currentState         = CONTEXT.get();
                JdbcTransactionState jdbcTransactionState = new JdbcTransactionState(currentState, propagation, dataSource.getConnection());
                jdbcTransactionState.setAutoCommit(false);
                CONTEXT.set(jdbcTransactionState);
                return jdbcTransactionState;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    @SneakyThrows
    @Override
    public void commit(TransactionState state)
    {
        if (state instanceof JdbcTransactionState jdbcTransactionState)
        {
            if (jdbcTransactionState.isAutoCommitFalseSet())
            {
                try
                {
                    jdbcTransactionState.findConnection().commit();
                }
                finally
                {
                    jdbcTransactionState.setAutoCommit(true);
                }
            }
            if (jdbcTransactionState.getConnection() != null)
            {
                jdbcTransactionState.getConnection().close();
            }
            CONTEXT.set(jdbcTransactionState.getParent());
        }
        else
        {
            throw new IllegalArgumentException("不支持的TransactionState");
        }
    }

    @SneakyThrows
    @Override
    public void rollback(TransactionState state, Throwable e)
    {
        if (state instanceof JdbcTransactionState jdbcTransactionState)
        {
            if (jdbcTransactionState.isAutoCommitFalseSet())
            {
                try
                {
                    jdbcTransactionState.findConnection().rollback();
                }
                finally
                {
                    jdbcTransactionState.setAutoCommit(true);
                }
            }
            if (jdbcTransactionState.getConnection() != null)
            {
                jdbcTransactionState.getConnection().close();
            }
            CONTEXT.set(jdbcTransactionState.getParent());
        }
        else
        {
            throw new IllegalArgumentException("不支持的TransactionState");
        }
    }
}
