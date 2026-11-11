package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Scene;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SceneManager {


    public static Scene getSceneById(int sceneId) {
        String query = "SELECT * FROM scenes WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        Scene scene = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sceneId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    scene = new Scene(
                        rs.getInt("id"),
                        rs.getInt("chapter_id"),
                        rs.getString("background_image"),
                        rs.getInt("scene_order")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scene;
    }

    public static int getFirstSceneId(int chapterId) {
        String query = "SELECT id FROM scenes WHERE chapter_id = ? ORDER BY scene_order ASC LIMIT 1";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Retornar -1 si no se encuentra
    }

    public static Scene getNextSceneInChapter(int currentSceneId, int chapterId) {
        String query = "SELECT * FROM scenes WHERE chapter_id = ? AND scene_order = (SELECT scene_order + 1 FROM scenes WHERE id = ?)";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        Scene nextScene = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);       // Capítulo actual
            stmt.setInt(2, currentSceneId);  // ID de la escena actual

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nextScene = new Scene(
                        rs.getInt("id"),
                        rs.getInt("chapter_id"),
                        rs.getString("background_image"),
                        rs.getInt("scene_order")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextScene;
    }

    public static void updateCurrentScene(int gameId, int sceneId) {
        String query = "UPDATE games SET current_scene_id = ? WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sceneId);
            stmt.setInt(2, gameId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Decisión actual actualizada para el juego con ID: " + gameId);
            } else {
                System.err.println("Error al actualizar la decisión actual para el juego con ID: " + gameId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static Scene getFirstSceneOfChapter( int chapterId) {
        String query = "SELECT * FROM scenes WHERE chapter_id = ? ORDER BY scene_order ASC LIMIT 1";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        Scene firstScene = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    firstScene = new Scene(
                        rs.getInt("id"),
                        rs.getInt("chapter_id"),
                        rs.getString("background_image"),
                        rs.getInt("scene_order")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return firstScene;
    }

    public static List<Scene> getScenesByChapterId(int chapterId) {
        List<Scene> scenes = new ArrayList<>();
        String query = "SELECT * FROM scenes WHERE chapter_id = ? ORDER BY scene_order ASC";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Scene scene = new Scene(
                        rs.getInt("id"),
                        rs.getInt("chapter_id"),
                        rs.getString("background_image"),
                        rs.getInt("scene_order")
                    );
                    scenes.add(scene);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scenes;
    }

    public static int createScene(Scene scene) {
        String sql = "INSERT INTO scenes (chapter_id, background_image, scene_order) VALUES (?, ?, ?)";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, scene.getChapterId());
            stmt.setString(2, scene.getBackgroundImage() != null ? scene.getBackgroundImage() : "default_bg.png");
            stmt.setInt(3, scene.getSceneOrder());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear la escena, ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Devuelve el ID generado
                } else {
                    throw new SQLException("No se pudo obtener el ID generado.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean updateScene(Scene scene) {
        String sql = "UPDATE scenes SET background_image = ?, scene_order = ? WHERE id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, scene.getBackgroundImage());
            stmt.setInt(2, scene.getSceneOrder());
            stmt.setInt(3, scene.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteScene(int sceneId) {
        String sql = "DELETE FROM scenes WHERE id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sceneId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }





}
