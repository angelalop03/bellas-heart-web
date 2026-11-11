package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Chapter;
import com.us.TFG.model.Game;
import com.us.TFG.model.PlayerChapterState;
import com.us.TFG.model.RelationshipState;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChapterManager {


    public static List<Chapter> getChaptersForPlayerandGame(int playerId, int gameId) {
        List<Chapter> unlockedChapters = new ArrayList<>();

        String query = "SELECT c.id, c.name, c.chapter_number, c.image_path, c.required_relationship_state " +
            "FROM chapters c " +
            "JOIN player_chapter_state pcs ON c.id = pcs.chapter_id " +
            "WHERE pcs.player_id = ? AND pcs.game_id= ? AND pcs.unlocked = true";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2,gameId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Chapter chapter = new Chapter();
                    chapter.setId(rs.getInt("id"));
                    chapter.setName(rs.getString("name"));
                    chapter.setChapterNumber(rs.getString("chapter_number"));
                    chapter.setImagePath(rs.getString("image_path"));
                    chapter.setRequiredRelationshipState(rs.getString("required_relationship_state"));

                    unlockedChapters.add(chapter);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unlockedChapters;
    }

    // Dentro de tu clase ChapterManager

    public static boolean isChapterPlayed(int gameId, int chapterId) {
        String query = "SELECT played FROM player_chapter_state WHERE game_id = ? AND chapter_id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, gameId);
            stmt.setInt(2, chapterId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("played");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Si no se encuentra el registro, se considera que no se ha jugado
    }


    public static int findFirstChapter(int historyId) {
        String query = "SELECT id FROM chapters WHERE history_id = ? ORDER BY chapter_number ASC LIMIT 1";

        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, historyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Retorna -1 si no se encuentra un capítulo
    }

    // Método para desbloquear el primer capítulo
    public static void unlockFirstChapter(int playerId, Game game, Integer historyId) {
        int firstChapterId = findFirstChapter(historyId);

        if (firstChapterId == -1) {
            System.err.println("No se encontró un capítulo inicial para la historia con ID: " + historyId);
            return;
        }

        game.setCurrentChapterId(firstChapterId);

        // Obtener la primera escena del capítulo
        int firstSceneId = SceneManager.getFirstSceneId(firstChapterId);
        SceneManager.updateCurrentScene(game.getId(), firstSceneId);

        // Obtener la primera decisión de la escena
        int firstDecisionId = DecisionManager.getFirstDecisionId(firstSceneId);
        DecisionManager.updateCurrentDecision(game.getId(), firstDecisionId);

        // Marcar el capítulo como desbloqueado en player_chapter_state
        PlayerChapterStateManager.unlockChapterForPlayer(playerId, game.getId(), firstChapterId);

        System.out.println("Primer capítulo desbloqueado y actualizado correctamente.");
    }

    public static void updateCurrentChapter(int gameId, int chapterId) {
        String query = "UPDATE games SET current_chapter_id = ? WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);
            stmt.setInt(2, gameId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void markChapterAsPlayed(int gameId, int chapterId) {
        String query = "UPDATE player_chapter_state SET played = true WHERE game_id = ? AND chapter_id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, gameId);
            stmt.setInt(2, chapterId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("El capítulo con ID " + chapterId + " ha sido marcado como jugado para el juego con ID " + gameId);
            } else {
                System.err.println("No se pudo marcar el capítulo como jugado. Verifique que el estado del capítulo existe en la base de datos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int unlockNextChapter(int gameId, int currentChapterId) {
        String nextChaptersQuery = "SELECT id, chapter_number, required_relationship_state FROM chapters " +
            "WHERE history_id = (SELECT history_id FROM games WHERE id = ?) " +
            "AND chapter_number > (SELECT chapter_number FROM chapters WHERE id = ?) " +
            "ORDER BY CAST(chapter_number AS DECIMAL) ASC";

        String currentChapterNumberQuery = "SELECT chapter_number FROM chapters WHERE id = ?";

        String insertUnlockQuery = "INSERT INTO player_chapter_state (player_id, chapter_id, game_id, played, unlocked) VALUES (?, ?, ?, false, true)";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        int nextChapterId = -1;

        try {
            // Obtener el número del capítulo actual
            PreparedStatement currentChapterStmt = connection.prepareStatement(currentChapterNumberQuery);
            currentChapterStmt.setInt(1, currentChapterId);
            ResultSet currentChapterRs = currentChapterStmt.executeQuery();
            String currentChapterNumber = null;

            if (currentChapterRs.next()) {
                currentChapterNumber = currentChapterRs.getString("chapter_number");
            }

            currentChapterRs.close();
            currentChapterStmt.close();

            if (currentChapterNumber == null) {
                throw new RuntimeException("No se pudo encontrar el capítulo actual.");
            }

            // Separar X.Y del capítulo actual
            String[] parts = currentChapterNumber.split("\\.");
            int currentGroup = Integer.parseInt(parts[0]);

            PreparedStatement nextChaptersStmt = connection.prepareStatement(nextChaptersQuery);
            nextChaptersStmt.setInt(1, gameId);
            nextChaptersStmt.setInt(2, currentChapterId);

            ResultSet rs = nextChaptersStmt.executeQuery();
            Game currentGame = GameManager.getGameById(gameId);
            RelationshipState currentRelationshipState = currentGame.getRelationshipState();

            while (rs.next()) {
                int candidateChapterId = rs.getInt("id");
                String candidateChapterNumber = rs.getString("chapter_number");
                String requiredRelationshipState = rs.getString("required_relationship_state");

                // Separar grupo del candidato
                String[] candidateParts = candidateChapterNumber.split("\\.");
                int candidateGroup = Integer.parseInt(candidateParts[0]);

                // Bloquear todos los demás capítulos del mismo grupo
                if (candidateGroup == currentGroup) {
                    continue;
                }

                // Verificar estado de relación
                if (requiredRelationshipState == null || currentRelationshipState.name().equals(requiredRelationshipState)) {
                    PreparedStatement insertUnlockStmt = connection.prepareStatement(insertUnlockQuery);
                    insertUnlockStmt.setInt(1, currentGame.getPlayerId());
                    insertUnlockStmt.setInt(2, candidateChapterId);
                    insertUnlockStmt.setInt(3, gameId);

                    int rowsInserted = insertUnlockStmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Capítulo " + candidateChapterNumber + " desbloqueado para el juego " + gameId);
                    } else {
                        System.err.println("El capítulo " + candidateChapterId + " ya estaba desbloqueado.");
                    }
                    insertUnlockStmt.close();

                    nextChapterId = candidateChapterId;
                    break;
                } else {
                    System.out.println("El estado de la relación actual (" + currentRelationshipState + ") no cumple con el requisito para desbloquear el capítulo " + candidateChapterId + " (" + requiredRelationshipState + ").");
                }
            }

            rs.close();
            nextChaptersStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextChapterId;
    }



    public static Chapter getChapterById(int chapterId) {
        Chapter chapter = null;
        String query = "SELECT * FROM chapters WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chapterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                chapter = new Chapter();
                chapter.setId(rs.getInt("id"));
                chapter.setName(rs.getString("name"));
                chapter.setChapterNumber(rs.getString("chapter_number"));
                chapter.setImagePath(rs.getString("image_path"));
                chapter.setRequiredRelationshipState(rs.getString("required_relationship_state"));
                chapter.setHistory(rs.getInt("history_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chapter;
    }

    public static Chapter getIntroChapterForHistory(int historyId) {
        String query = "SELECT * FROM chapters WHERE history_id = ? AND chapter_number = '0' LIMIT 1";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, historyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Chapter chapter = new Chapter();
                chapter.setId(rs.getInt("id"));
                chapter.setName(rs.getString("name"));
                chapter.setChapterNumber(rs.getString("chapter_number"));
                chapter.setImagePath(rs.getString("image_path"));
                chapter.setRequiredRelationshipState(rs.getString("required_relationship_state"));
                chapter.setHistory(rs.getInt("history_id"));
                return chapter;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<Chapter> getChaptersByHistoryId(int historyId) {
        List<Chapter> chapters = new ArrayList<>();

        String query = "SELECT * FROM chapters WHERE history_id = ? ORDER BY CAST(chapter_number AS DECIMAL) ASC";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, historyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Chapter chapter = new Chapter();
                chapter.setId(rs.getInt("id"));
                chapter.setName(rs.getString("name"));
                chapter.setChapterNumber(rs.getString("chapter_number"));
                chapter.setImagePath(rs.getString("image_path"));
                chapter.setRequiredRelationshipState(rs.getString("required_relationship_state"));
                chapter.setHistory(rs.getInt("history_id"));

                chapters.add(chapter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chapters;
    }

    public static boolean createChapter(Chapter chapter) {
        String query = "INSERT INTO chapters (name, chapter_number, image_path, required_relationship_state, history_id) " +
            "VALUES (?, ?, ?, ?, ?)";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, chapter.getName());
            stmt.setString(2, chapter.getChapterNumber());
            stmt.setString(3, chapter.getImagePath());
            stmt.setString(4, chapter.getRequiredRelationshipState());
            stmt.setInt(5, chapter.getHistory());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateChapter(Chapter chapter) {
        String query = "UPDATE chapters SET name = ?, chapter_number = ?, required_relationship_state = ? WHERE id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, chapter.getName());
            stmt.setString(2, chapter.getChapterNumber());

            // Manejar null explícitamente para required_relationship_state
            if (chapter.getRequiredRelationshipState() == null || chapter.getRequiredRelationshipState().equalsIgnoreCase("null")) {
                stmt.setNull(3, com.us.TFG.db.Types.VARCHAR);
            } else {
                stmt.setString(3, chapter.getRequiredRelationshipState());
            }

            stmt.setInt(4, chapter.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteChapter(int chapterId) {
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            connection.setAutoCommit(false); // Iniciar transacción

            // Eliminar escenas asociadas al capítulo
            String deleteScenesSql = "DELETE FROM scenes WHERE chapter_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteScenesSql)) {
                stmt.setInt(1, chapterId);
                stmt.executeUpdate();
            }

            // Eliminar el capítulo
            String deleteChapterSql = "DELETE FROM chapters WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteChapterSql)) {
                stmt.setInt(1, chapterId);
                stmt.executeUpdate();
            }

            connection.commit(); // Confirmar los cambios
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback(); // Revertir cambios en caso de error
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
