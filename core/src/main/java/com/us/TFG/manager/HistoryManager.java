package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.History;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    public static List<History> getHistories() {
        List<History> histories = new ArrayList<>();
        String query = "SELECT id, title, published, image_path, description FROM historys";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    History history = new History();
                    history.setId(rs.getInt("id"));
                    history.setTitle(rs.getString("title"));
                    history.setPublished(rs.getBoolean("published"));
                    history.setImagePath(rs.getString("image_path"));
                    history.setDescription(rs.getString("description"));


                    histories.add(history);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return histories;
    }

    public static boolean createHistory(History history) {
        String sql = "INSERT INTO historys (title, published, image_path, description) VALUES (?, ?, ?, ?)";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");

        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, history.getTitle());
            stmt.setBoolean(2, history.isPublished());
            stmt.setString(3, history.getImagePath());
            stmt.setString(4, history.getDescription());
            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<History> getHistoriesPublished() {
        List<History> histories = new ArrayList<>();
        String query = "SELECT id, title, image_path, description FROM historys WHERE published = 1";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    History history = new History();
                    history.setId(rs.getInt("id"));
                    history.setTitle(rs.getString("title"));
                    history.setImagePath(rs.getString("image_path"));
                    history.setDescription(rs.getString("description"));
                    history.setPublished(true); // Ya que solo trae las publicadas

                    histories.add(history);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return histories;
    }

    public static void updateHistory(int historyId, String newTitle, String newDescription) {
        String sql = "UPDATE historys SET title = ?, description = ? WHERE id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setString(2, newDescription);
            stmt.setInt(3, historyId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setPublished(int historyId, boolean published) {
        String sql = "UPDATE historys SET published = ? WHERE id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, published);
            stmt.setInt(2, historyId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static History getHistoryByChapterId(int chapterId) {
        String query = "SELECT h.id, h.title, h.published, h.image_path, h.description " +
            "FROM historys h " +
            "JOIN chapters c ON h.id = c.history_id " +
            "WHERE c.id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    History history = new History();
                    history.setId(rs.getInt("id"));
                    history.setTitle(rs.getString("title"));
                    history.setPublished(rs.getBoolean("published"));
                    history.setImagePath(rs.getString("image_path"));
                    history.setDescription(rs.getString("description"));
                    return history;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // No se encontró la historia
    }

    public static void deleteHistory(int historyId) {
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            connection.setAutoCommit(false); // Iniciar transacción

            // Eliminar capítulos asociados
            String deleteChaptersSql = "DELETE FROM chapters WHERE history_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteChaptersSql)) {
                stmt.setInt(1, historyId);
                stmt.executeUpdate();
            }

            // Eliminar la historia
            String deleteHistorySql = "DELETE FROM historys WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteHistorySql)) {
                stmt.setInt(1, historyId);
                stmt.executeUpdate();
            }

            connection.commit(); // Confirmar la transacción
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback(); // Revertir cambios si hay error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }








}
