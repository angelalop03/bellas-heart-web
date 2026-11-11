package com.us.TFG.db;

import java.util.Date;

/** Sentencia SQL con parámetros. Imita java.sql.PreparedStatement. */
public interface PreparedStatement extends Statement {
    void setInt(int index, int value) throws SQLException;

    void setLong(int index, long value) throws SQLException;

    void setString(int index, String value) throws SQLException;

    void setBoolean(int index, boolean value) throws SQLException;

    /** Guarda la fecha como milisegundos desde epoch, igual que hace el driver JDBC de SQLite. */
    void setDate(int index, Date value) throws SQLException;

    void setNull(int index, int sqlType) throws SQLException;

    ResultSet executeQuery() throws SQLException;

    int executeUpdate() throws SQLException;

    boolean execute() throws SQLException;

    ResultSet getGeneratedKeys() throws SQLException;
}
