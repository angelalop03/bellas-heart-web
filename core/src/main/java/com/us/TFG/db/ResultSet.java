package com.us.TFG.db;

import java.util.Date;

/** Resultado de una consulta. Imita java.sql.ResultSet (índices de columna desde 1). */
public interface ResultSet extends AutoCloseable {
    boolean next() throws SQLException;

    int getInt(String column) throws SQLException;

    int getInt(int columnIndex) throws SQLException;

    long getLong(String column) throws SQLException;

    String getString(String column) throws SQLException;

    String getString(int columnIndex) throws SQLException;

    boolean getBoolean(String column) throws SQLException;

    Object getObject(String column) throws SQLException;

    Date getDate(String column) throws SQLException;

    boolean wasNull() throws SQLException;

    @Override
    void close() throws SQLException;
}
