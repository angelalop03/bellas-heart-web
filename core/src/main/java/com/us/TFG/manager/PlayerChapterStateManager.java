package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.SQLException;

public class PlayerChapterStateManager {

    // Método para actualizar player_chapter_state a unlocked = true
    public static void unlockChapterForPlayer(int playerId, int gameId, int chapterId) {
        String query = "INSERT INTO player_chapter_state (player_id, chapter_id, game_id, played, unlocked) " +
            "VALUES (?, ?, ?, ?, ?)";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, chapterId);
            stmt.setInt(3, gameId);
            stmt.setBoolean(4, false); // No jugado aún
            stmt.setBoolean(5, true); // Desbloqueado

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("El capítulo con ID " + chapterId + " ha sido desbloqueado para el juego con ID " + gameId);
            } else {
                System.err.println("Error al actualizar el estado del capítulo en player_chapter_state.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
