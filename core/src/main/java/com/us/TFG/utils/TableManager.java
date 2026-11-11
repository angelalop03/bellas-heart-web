package com.us.TFG.utils;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

import java.lang.reflect.Field;
import com.us.TFG.db.Connection;
import com.us.TFG.db.SQLException;
import com.us.TFG.db.Statement;

public class TableManager {
    // Método para crear una tabla a partir de una clase con la anotación @Table
    public static void createTable(Connection connection, Class<?> clazz) throws SQLException {
        // Verificar si la clase tiene la anotación @Table
        if (clazz.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            String tableName = tableAnnotation.name();
            StringBuilder createSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                // Verificar si el campo tiene la anotación @Column
                if (field.isAnnotationPresent(Column.class)) {
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    String columnName = columnAnnotation.name();
                    String columnType = columnAnnotation.type();

                    createSQL.append(columnName).append(" ").append(columnType);

                    if (columnAnnotation.notNull()) {
                        createSQL.append(" NOT NULL");
                    }
                    if (columnAnnotation.primaryKey()) {
                        createSQL.append(" PRIMARY KEY AUTOINCREMENT");
                    }
                    if (columnAnnotation.unique()) {
                        createSQL.append(" UNIQUE");
                    }
                    if (!columnAnnotation.defaultValue().isEmpty()) {
                        createSQL.append(" DEFAULT ").append(columnAnnotation.defaultValue());
                    }

                    if (i < fields.length - 1) {
                        createSQL.append(", ");
                    }
                }
            }
            createSQL.append(");");

            // Ejecutar la sentencia SQL
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createSQL.toString());
                System.out.println("Tabla '" + tableName + "' creada correctamente.");
            }
        } else {
            throw new RuntimeException("La clase " + clazz.getSimpleName() + " no tiene la anotación @Table.");
        }
    }


}
