package com.us.TFG.web;

import com.us.TFG.db.Connection;
import com.us.TFG.db.DatabaseProvider;

/** Base de datos SQLite dentro del navegador (sql.js), persistida en IndexedDB. */
public class SqlJsDatabaseProvider implements DatabaseProvider {
    @Override
    public Connection open() {
        return new SqlJsConnection();
    }

    @Override
    public boolean isNewDatabase() {
        return SqlJsBridge.isNew();
    }
}
