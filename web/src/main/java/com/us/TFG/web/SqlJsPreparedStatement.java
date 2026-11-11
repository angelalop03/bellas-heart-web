package com.us.TFG.web;

import com.badlogic.gdx.utils.JsonValue;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Sentencia que se ejecuta en sql.js. Los parámetros se envían como un array JSON. */
class SqlJsPreparedStatement implements PreparedStatement {
    private final SqlJsConnection connection;
    private final String sql;
    /** Valores ya codificados en JSON (número, "texto" o null), por posición (1..n). */
    private final List<String> params = new ArrayList<>();
    private long lastInsertId = -1;

    SqlJsPreparedStatement(SqlJsConnection connection, String sql) {
        this.connection = connection;
        this.sql = sql;
    }

    private void set(int index, String jsonValue) {
        while (params.size() < index) {
            params.add("null");
        }
        params.set(index - 1, jsonValue);
    }

    private String paramsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(params.get(i));
        }
        return sb.append(']').toString();
    }

    static String quote(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int k = hex.length(); k < 4; k++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    @Override
    public void setInt(int index, int value) {
        set(index, Integer.toString(value));
    }

    @Override
    public void setLong(int index, long value) {
        set(index, Long.toString(value));
    }

    @Override
    public void setString(int index, String value) {
        set(index, value == null ? "null" : quote(value));
    }

    @Override
    public void setBoolean(int index, boolean value) {
        set(index, value ? "1" : "0");
    }

    @Override
    public void setDate(int index, Date value) {
        set(index, value == null ? "null" : Long.toString(value.getTime()));
    }

    @Override
    public void setNull(int index, int sqlType) {
        set(index, "null");
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return executeQuery(sql);
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeUpdate(sql);
    }

    @Override
    public boolean execute() throws SQLException {
        return execute(sql);
    }

    @Override
    public ResultSet getGeneratedKeys() {
        return SqlJsConnection.singleValue("last_insert_rowid()", lastInsertId);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        String trimmed = sql.trim().toUpperCase();
        if (trimmed.startsWith("SELECT") || trimmed.startsWith("PRAGMA") || trimmed.startsWith("WITH")) {
            executeQuery(sql);
            return true;
        }
        executeUpdate(sql);
        return false;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        connection.beforeStatement();
        JsonValue result = SqlJsConnection.parse(SqlJsBridge.run(sql, paramsJson()));
        lastInsertId = result.getLong("lastId", -1);
        return result.getInt("changes", 0);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        connection.beforeStatement();
        return new SqlJsResultSet(SqlJsConnection.parse(SqlJsBridge.query(sql, paramsJson())));
    }

    @Override
    public void close() {
    }
}
