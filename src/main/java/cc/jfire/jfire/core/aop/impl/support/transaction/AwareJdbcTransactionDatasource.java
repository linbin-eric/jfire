package cc.jfire.jfire.core.aop.impl.support.transaction;

import lombok.Data;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 供其他体系的持久化框架使用，使得其他体系的框架在需要获取 Connection 的场合，能够感知到当前的事务存在与否。
 * 如果存在，则返回事务中已经存在的链接来使用；
 * 如果不存在，则创建新的链接。
 */
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
            throw new IllegalStateException("当前不存在事务，不能获取链接");
        }
        else
        {
            return new AwareJdbcTransactionConnection(currentState.findConnection());
        }
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
