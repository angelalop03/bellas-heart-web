package com.us.TFG.manager;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.us.TFG.MyTfgGame;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsManager {

    // Método para obtener todas las noticias de la base de datos
    public static List<NewsData> getAllNews() {
        String query = "SELECT id, name, text FROM news";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        List<NewsData> newsList = new ArrayList<>();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                newsList.add(new NewsData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("text")
                ));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newsList;
    }

    // Método para eliminar una noticia por su ID
    public static boolean deleteNews(int newsId) {
        String query = "DELETE FROM news WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, newsId);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0; // Retorna true si se eliminó una fila
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Retorna false si ocurrió un error
    }

    public static boolean updateNews(int newsId, String newTitle, String newText) {
        String query = "UPDATE news SET name = ?, text = ? WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, newTitle);
            stmt.setString(2, newText);
            stmt.setInt(3, newsId);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0; // Retorna true si se actualizó al menos una fila
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Retorna false si ocurrió un error
    }


    // Método para crear una nueva noticia
    public static boolean createNews(String title, String text) {
        String query = "INSERT INTO news (name, text) VALUES (?, ?)";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, text);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0; // Retorna true si se insertó al menos una fila
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Retorna false si ocurrió un error
    }

    // Clase auxiliar para encapsular los datos de una noticia
    public static class NewsData {
        private final int id;
        private final String name;
        private final String text;

        public NewsData(int id, String name, String text) {
            this.id = id;
            this.name = name;
            this.text = text;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

    }
}
