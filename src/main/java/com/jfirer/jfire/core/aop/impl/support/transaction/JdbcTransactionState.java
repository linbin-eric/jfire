package com.jfirer.jfire.core.aop.impl.support.transaction;

import lombok.Data;

import java.sql.Connection;
import java.sql.SQLException;

@Data
public class JdbcTransactionState implements TransactionState
{
    private final JdbcTransactionState parent;
    private final Propagation          propagation;
    private final Connection           connection;
    private       boolean              autoCommitFalseSet = false;

    @Override
    public Propagation propagation()
    {
        return propagation;
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        if (autoCommit == false)
        {
            autoCommitFalseSet = true;
        }
        else
        {
            autoCommitFalseSet = false;
        }
        findConnection().setAutoCommit(autoCommit);
    }


    public Connection findConnection()
    {
        return connection == null ? parent.findConnection() : connection;
    }
}
