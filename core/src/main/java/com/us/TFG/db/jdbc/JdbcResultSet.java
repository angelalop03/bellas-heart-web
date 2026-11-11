package com.us.TFG.db.jdbc;

import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;

import java.util.Date;

class JdbcResultSet implements ResultSet {
    private final java.sql.ResultSet delegate;

    JdbcResultSet(java.sql.ResultSet delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean next() throws SQLException {
        return JdbcConnection.call(delegate::next);
    }

    @Override
    public int getInt(String column) throws SQLException {
        return JdbcConnection.call(() -> delegate.getInt(column));
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return JdbcConnection.call(() -> delegate.getInt(columnIndex));
    }

    @Override
    public long getLong(String column) throws SQLException {
        return JdbcConnection.call(() -> delegate.getLong(column));
    }

    @Override
    public String getString(String column) throws SQLException {
        return JdbcConnection.call(() -> delegate.getString(column));
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return JdbcConnection.call(() -> delegate.getString(columnIndex));
    }

    @Override
    public boolean getBoolean(String column) throws SQLException {
        return JdbcConnection.call(() -> delegate.getBoolean(column));
    }

    @Override
    public Object getObject(String column) throws SQLException {
        return JdbcConnection.call(() -> delegate.getObject(column));
    }

    @Override
    public Date getDate(String column) throws SQLException {
        java.sql.Date date = JdbcConnection.call(() -> delegate.getDate(column));
        return date == null ? null : new Date(date.getTime());
    }

    @Override
    public boolean wasNull() throws SQLException {
        return JdbcConnection.call(delegate::wasNull);
    }

    @Override
    public void close() throws SQLException {
        JdbcConnection.run(delegate::close);
    }
}
