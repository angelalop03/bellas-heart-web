package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.PlayerDecisionState;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PlayerDecisionStateManager {


        public static void updateDecisionState(int playerId, int gameId, int decisionId, int optionId, boolean completed) {
            String query = "INSERT INTO player_decision_state (player_id, game_id, decision_id, option_id, completed) VALUES (?,?,?,?,?)";
            Connection connection = MyTfgGame.databaseManager.getConnection();

            if (connection == null) {
                throw new RuntimeException("No se pudo conectar a la base de datos.");
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, playerId);      // ID del jugador
                stmt.setInt(2, gameId);        // ID del juego
                stmt.setInt(3, decisionId);    // ID de la decisión
                stmt.setInt(4, optionId);      // ID de la opción elegida
                stmt.setBoolean(5, completed); // Marcar como completada o no


                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Estado de la decisión actualizado correctamente.");
                } else {
                    System.out.println("No se encontró ninguna decisión para actualizar.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static List<PlayerDecisionState> getPositivePointDecisions(int playerId, int gameId) {
            List<PlayerDecisionState> decisions = new ArrayList<>();
            String query = "SELECT pds.* FROM player_decision_state pds " +
                "JOIN options o ON pds.option_id = o.id " +
                "WHERE pds.player_id = ? AND pds.game_id = ? AND (o.points_m > 0 OR o.points_r > 0)";
            Connection connection = MyTfgGame.databaseManager.getConnection();

            if (connection == null) {
                throw new RuntimeException("No se pudo conectar a la base de datos.");
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, playerId);
                stmt.setInt(2, gameId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    PlayerDecisionState decision = new PlayerDecisionState();
                    decision.setId(rs.getInt("id"));
                    decision.setPlayerId(rs.getInt("player_id"));
                    decision.setGameId(rs.getInt("game_id"));
                    decision.setDecisionId(rs.getInt("decision_id"));
                    decision.setOptionId(rs.getInt("option_id"));
                    decision.setCompleted(rs.getBoolean("completed"));
                    decisions.add(decision);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return decisions;
        }




    }


