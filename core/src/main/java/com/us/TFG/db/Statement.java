package com.us.TFG.db;

/** Sentencia SQL sin parámetros. Imita java.sql.Statement. */
public interface Statement extends AutoCloseable {
    int RETURN_GENERATED_KEYS = 1;
    int NO_GENERATED_KEYS = 2;

    boolean execute(String sql) throws SQLException;

    int executeUpdate(String sql) throws SQLException;

    ResultSet executeQuery(String sql) throws SQLException;

    @Override
    void close() throws SQLException;
}
