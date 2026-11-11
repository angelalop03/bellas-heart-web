package com.us.TFG.web;

import com.badlogic.gdx.utils.JsonValue;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;

import java.util.Date;

/** Resultado de una consulta de sql.js: {"columns":[...],"values":[[...],...]}. */
class SqlJsResultSet implements ResultSet {
    private final String[] columns;
    private final JsonValue rows;
    private JsonValue currentRow;
    private boolean started;
    private boolean lastWasNull;

    SqlJsResultSet(JsonValue result) {
        JsonValue cols = result.get("columns");
        columns = new String[cols == null ? 0 : cols.size];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = cols.getString(i);
        }
        rows = result.get("values");
    }

    @Override
    public boolean next() {
        if (rows == null) return false;
        currentRow = started ? (currentRow == null ? null : currentRow.next) : rows.child;
        started = true;
        return currentRow != null;
    }

    private int indexOf(String column) throws SQLException {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(column)) return i;
        }
        // Como en JDBC: "tabla.columna" también se resuelve por el nombre de la columna.
        int dot = column.lastIndexOf('.');
        if (dot >= 0) return indexOf(column.substring(dot + 1));
        throw new SQLException("Columna no encontrada: " + column);
    }

    private JsonValue value(int index) throws SQLException {
        if (currentRow == null) throw new SQLException("No hay fila actual");
        JsonValue v = currentRow.get(index);
        lastWasNull = v == null || v.isNull();
        return lastWasNull ? null : v;
    }

    private static long toLong(JsonValue v) {
        if (v == null) return 0;
        if (v.isLong()) return v.asLong();
        if (v.isDouble()) return (long) v.asDouble();
        if (v.isBoolean()) return v.asBoolean() ? 1 : 0;
        String s = v.asString().trim();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            try {
                return (long) Double.parseDouble(s);
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    @Override
    public int getInt(String column) throws SQLException {
        return getInt(indexOf(column) + 1);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return (int) toLong(value(columnIndex - 1));
    }

    @Override
    public long getLong(String column) throws SQLException {
        return toLong(value(indexOf(column)));
    }

    @Override
    public String getString(String column) throws SQLException {
        return getString(indexOf(column) + 1);
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        JsonValue v = value(columnIndex - 1);
        if (v == null) return null;
        if (v.isLong()) return Long.toString(v.asLong());
        return v.asString();
    }

    @Override
    public boolean getBoolean(String column) throws SQLException {
        JsonValue v = value(indexOf(column));
        if (v == null) return false;
        if (v.isString()) {
            String s = v.asString().trim();
            return s.equalsIgnoreCase("true") || s.equals("1");
        }
        return toLong(v) != 0;
    }

    @Override
    public Object getObject(String column) throws SQLException {
        JsonValue v = value(indexOf(column));
        if (v == null) return null;
        if (v.isLong()) return v.asLong();
        if (v.isDouble()) return v.asDouble();
        if (v.isBoolean()) return v.asBoolean();
        return v.asString();
    }

    @Override
    public Date getDate(String column) throws SQLException {
        JsonValue v = value(indexOf(column));
        if (v == null) return null;
        if (v.isString()) {
            String s = v.asString().trim();
            // Formato texto de SQLite: yyyy-MM-dd[ HH:mm:ss]
            if (s.length() >= 10 && s.charAt(4) == '-') {
                int year = Integer.parseInt(s.substring(0, 4));
                int month = Integer.parseInt(s.substring(5, 7));
                int day = Integer.parseInt(s.substring(8, 10));
                @SuppressWarnings("deprecation")
                Date date = new Date(year - 1900, month - 1, day);
                return date;
            }
        }
        return new Date(toLong(v));
    }

    @Override
    public boolean wasNull() {
        return lastWasNull;
    }

    @Override
    public void close() {
    }
}
