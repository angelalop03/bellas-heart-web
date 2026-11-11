package com.us.TFG.db.jdbc;

import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;

import java.util.Date;

class JdbcPreparedStatement extends JdbcStatement implements PreparedStatement {
    private final java.sql.PreparedStatement delegate;

    JdbcPreparedStatement(java.sql.PreparedStatement delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public void setInt(int index, int value) throws SQLException {
        JdbcConnection.run(() -> delegate.setInt(index, value));
    }

    @Override
    public void setLong(int index, long value) throws SQLException {
        JdbcConnection.run(() -> delegate.setLong(index, value));
    }

    @Override
    public void setString(int index, String value) throws SQLException {
        JdbcConnection.run(() -> delegate.setString(index, value));
    }

    @Override
    public void setBoolean(int index, boolean value) throws SQLException {
        JdbcConnection.run(() -> delegate.setBoolean(index, value));
    }

    @Override
    public void setDate(int index, Date value) throws SQLException {
        JdbcConnection.run(() -> delegate.setDate(index, value == null ? null : new java.sql.Date(value.getTime())));
    }

    @Override
    public void setNull(int index, int sqlType) throws SQLException {
        JdbcConnection.run(() -> delegate.setNull(index, sqlType));
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return new JdbcResultSet(JdbcConnection.call(delegate::executeQuery));
    }

    @Override
    public int executeUpdate() throws SQLException {
        return JdbcConnection.call(delegate::executeUpdate);
    }

    @Override
    public boolean execute() throws SQLException {
        return JdbcConnection.call(delegate::execute);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return new JdbcResultSet(JdbcConnection.call(delegate::getGeneratedKeys));
    }
}
