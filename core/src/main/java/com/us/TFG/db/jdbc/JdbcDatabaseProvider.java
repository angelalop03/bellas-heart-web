package com.us.TFG.db.jdbc;

import com.us.TFG.db.Connection;
import com.us.TFG.db.DatabaseProvider;
import com.us.TFG.db.SQLException;

import java.io.File;

/** Base de datos SQLite en un fichero local (versión de escritorio y tests). */
public class JdbcDatabaseProvider implements DatabaseProvider {
    private final File dbFile;
    private boolean isNew;

    public JdbcDatabaseProvider(File dbFile) {
        this.dbFile = dbFile;
    }

    /** Ruta usada por la versión de escritorio original: APPDATA/BellasHeart/game.db. */
    public static JdbcDatabaseProvider desktopDefault() {
        String base = System.getenv("APPDATA");
        if (base == null) {
            base = System.getProperty("user.home");
        }
        return new JdbcDatabaseProvider(new File(base, "BellasHeart" + File.separator + "game.db"));
    }

    @Override
    public Connection open() throws SQLException {
        isNew = !dbFile.exists();
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return JdbcConnection.open("jdbc:sqlite:" + dbFile.getPath());
    }

    @Override
    public boolean isNewDatabase() {
        return isNew;
    }
}
