package com.jfirer.jfire.core.aop.impl.support.transaction;

import lombok.Data;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

@Data
public class AwareJdbcTransactionDatasource implements DataSource
{
    private final DataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException
    {
        JdbcTransactionState currentState = JdbcTransactionManager.CONTEXT.get();
        if (currentState == null)
        {
            throw new IllegalStateException("当前上下文中没有事务对象，无法获取链接");
        }
        return new AwareJdbcTransactionConnection(currentState.findConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return dataSource.isWrapperFor(iface);
    }
}
