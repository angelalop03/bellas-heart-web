package com.us.TFG.utils;

import com.badlogic.gdx.Gdx;
import com.us.TFG.db.Connection;
import com.us.TFG.db.DatabaseProvider;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.SQLException;
import com.us.TFG.db.jdbc.JdbcConnection;
import com.us.TFG.db.jdbc.JdbcDatabaseProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;


public class DatabaseManager {

    private Connection connection;

    /** Versión de escritorio: SQLite en APPDATA/BellasHeart/game.db. */
    public DatabaseManager() {
        this(JdbcDatabaseProvider.desktopDefault());
    }

    /** Cada plataforma (escritorio o navegador) indica cómo se abre la base de datos. */
    public DatabaseManager(DatabaseProvider provider) {
        try {
            connection = provider.open();
            System.out.println("Conexión a la base de datos establecida.");

            if (provider.isNewDatabase()) {
                System.out.println("Base de datos no existe. Se crean las tablas y se cargan los datos iniciales.");
                executeManualSQL("schema.sql");
                executeManualSQL("data.sql");
            } else {
                System.out.println("Base de datos ya existente. No se ejecuta SQL de inicialización.");
            }

        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    private void executeManualSQL(String path) {
        try {
            BufferedReader reader;

            if (Gdx.files != null) {
                // En modo juego
                reader = new BufferedReader(new StringReader(Gdx.files.internal(path).readString("UTF-8")));
            } else {
                // En modo test
                File file = new File("../assets/" + path);
                if (!file.exists()) {
                    System.err.println("Archivo SQL no encontrado: assets/" + path);
                    return;
                }
                reader = new BufferedReader(new FileReader(file));
            }

            // Todo el script en una sola transacción: mucho más rápido, sobre todo en el navegador.
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;

                sql.append(line);
                if (line.trim().endsWith(";")) {
                    try (PreparedStatement pstmt = connection.prepareStatement(sql.toString().trim())) {
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.err.println("Error en la sentencia SQL: " + sql);
                        e.printStackTrace();
                    }
                    sql.setLength(0);
                }
            }

            reader.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (IOException | SQLException e) {
            System.err.println("Error al ejecutar el archivo SQL manual: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para devolver la conexión
    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /** Usado por los tests para apuntar a una base de datos de pruebas (solo escritorio). */
    public void connect(String url) {
        try {
            this.connection = JdbcConnection.open(url);
            System.out.println("Conectado a: " + url);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al conectar a la base de datos: " + url, e);
        }
    }
}
