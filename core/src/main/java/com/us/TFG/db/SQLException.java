package com.us.TFG.db;

/** Excepción de base de datos independiente de la plataforma (sustituye a java.sql.SQLException). */
public class SQLException extends Exception {
    public SQLException(String message) {
        super(message);
    }

    public SQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public SQLException(Throwable cause) {
        super(cause);
    }
}
