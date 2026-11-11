package com.us.TFG.db;

/** Cada plataforma (escritorio, web) aporta su forma de abrir la base de datos SQLite. */
public interface DatabaseProvider {
    /** Abre la conexión. */
    Connection open() throws SQLException;

    /** true si la base de datos no existía y hay que cargar los datos iniciales (data.sql). */
    boolean isNewDatabase();
}
