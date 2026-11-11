package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Decision;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DecisionManager {
    public static Decision getFirstDecisionBySceneId(int sceneId) {
        String query = "SELECT * FROM decisions WHERE scene_id = ? ORDER BY decision_order ASC LIMIT 1";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        Decision decision = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, sceneId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                decision = new Decision(
                    rs.getInt("id"),
                    rs.getInt("scene_id"),
                    rs.getInt("decision_order"),
                    rs.getInt("character_id"),
                    rs.getString("dialogue")
                );
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return decision;
    }

    // Método para obtener la primera decisión de una escena
    public static int getFirstDecisionId(int sceneId) {
        String query = "SELECT id FROM decisions WHERE scene_id = ? ORDER BY decision_order ASC LIMIT 1";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sceneId);
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


    public static void updateCurrentDecision(int gameId, int decisionId) {
        String query = "UPDATE games SET current_decision_id = ? WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, decisionId);
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
    private static Decision mapDecisionFromResultSet(ResultSet rs) throws SQLException {
        Decision decision = new Decision();
        decision.setId(rs.getInt("id"));
        decision.setSceneId(rs.getInt("scene_id"));
        decision.setDecisionOrder(rs.getInt("decision_order"));
        decision.setDialogue(rs.getString("dialogue"));
        decision.setCharacterId(rs.getInt("character_id"));
        return decision;
    }

    public static Decision getDecisionById(int decisionId) {
        String query = "SELECT id, scene_id, character_id, dialogue, decision_order FROM decisions WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, decisionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Decision decision = new Decision();
                    decision.setId(rs.getInt("id"));
                    decision.setSceneId(rs.getInt("scene_id"));
                    decision.setCharacterId(rs.getInt("character_id"));
                    decision.setDialogue(rs.getString("dialogue"));
                    decision.setDecisionOrder(rs.getInt("decision_order"));
                    return decision;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static List<Decision> getDecisionsBySceneId(int sceneId) {
        List<Decision> decisions = new ArrayList<>();
        String query = "SELECT * FROM decisions WHERE scene_id = ? ORDER BY decision_order ASC";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sceneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    decisions.add(new Decision(
                        rs.getInt("id"),
                        rs.getInt("scene_id"),
                        rs.getInt("decision_order"),
                        rs.getInt("character_id"),
                        rs.getString("dialogue")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return decisions;
    }

    public static int createDecision(Decision decision) {
        String query = "INSERT INTO decisions (scene_id, decision_order, character_id, dialogue) VALUES (?, ?, ?, ?)";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, decision.getSceneId());
            stmt.setInt(2, decision.getDecisionOrder());
            stmt.setInt(3, decision.getCharacterId());
            stmt.setString(4, decision.getDialogue());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La creación de la decisión falló, no se insertó ninguna fila.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("La creación de la decisión falló, no se obtuvo el ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean updateDecision(Decision decision) {
        String query = "UPDATE decisions SET dialogue = ?, character_id = ?, decision_order = ? WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, decision.getDialogue());
            stmt.setInt(2, decision.getCharacterId());
            stmt.setInt(3, decision.getDecisionOrder());
            stmt.setInt(4, decision.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean deleteDecision(int decisionId) {
        String deleteOptionsQuery = "DELETE FROM options WHERE decision_id = ?";
        String deleteDecisionQuery = "DELETE FROM decisions WHERE id = ?";

        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            // Inicia la transacción
            connection.setAutoCommit(false);

            // Eliminar las opciones asociadas
            try (PreparedStatement deleteOptionsStmt = connection.prepareStatement(deleteOptionsQuery)) {
                deleteOptionsStmt.setInt(1, decisionId);
                deleteOptionsStmt.executeUpdate();
            }

            // Eliminar la decisión
            try (PreparedStatement deleteDecisionStmt = connection.prepareStatement(deleteDecisionQuery)) {
                deleteDecisionStmt.setInt(1, decisionId);
                int rows = deleteDecisionStmt.executeUpdate();

                if (rows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




}


