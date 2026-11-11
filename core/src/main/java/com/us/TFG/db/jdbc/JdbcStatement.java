package com.us.TFG.db.jdbc;

import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import com.us.TFG.db.Statement;

class JdbcStatement implements Statement {
    private final java.sql.Statement delegate;

    JdbcStatement(java.sql.Statement delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return JdbcConnection.call(() -> delegate.execute(sql));
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return JdbcConnection.call(() -> delegate.executeUpdate(sql));
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return new JdbcResultSet(JdbcConnection.call(() -> delegate.executeQuery(sql)));
    }

    @Override
    public void close() throws SQLException {
        JdbcConnection.run(delegate::close);
    }
}
