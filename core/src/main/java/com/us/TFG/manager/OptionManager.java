package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Game;
import com.us.TFG.model.Option;
import com.us.TFG.model.PsychologicalState;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OptionManager {

    public static List<Option> getOptionsByDecisionId(int decisionId) {
        String query = "SELECT id, decision_id, text, points_m, points_r, next_decision_id, required_psychological_state " +
            "FROM options WHERE decision_id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        List<Option> optionsList = new ArrayList<>();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, decisionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Option option = new Option();
                    option.setId(rs.getInt("id"));
                    option.setDecisionId(rs.getInt("decision_id"));
                    option.setText(rs.getString("text"));
                    option.setPointsM(rs.getInt("points_m"));
                    option.setPointsR(rs.getInt("points_r"));
                    option.setNextDecisionId(rs.getObject("next_decision_id") != null ? rs.getInt("next_decision_id") : null);
                    option.setRequiredPsychologicalState(rs.getString("required_psychological_state"));

                    optionsList.add(option);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return optionsList;
    }

    public static List<Option> getAvailableOptions(int currentDecisionId, Game game) {
        String query = "SELECT id, decision_id, text, points_m, points_r, next_decision_id, required_psychological_state " +
            "FROM options WHERE decision_id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        List<Option> optionsList = new ArrayList<>();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        PsychologicalState currentState = game.getPsychologicalState();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, currentDecisionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String requiredStateStr = rs.getString("required_psychological_state");

                    // Si es null o coincide con el estado psicológico actual, se añade
                    if (requiredStateStr == null || requiredStateStr.equals(currentState.name())) {
                        Option option = new Option();
                        option.setId(rs.getInt("id"));
                        option.setDecisionId(rs.getInt("decision_id"));
                        option.setText(rs.getString("text"));
                        option.setPointsM(rs.getInt("points_m"));
                        option.setPointsR(rs.getInt("points_r"));
                        option.setNextDecisionId(rs.getObject("next_decision_id") != null ? rs.getInt("next_decision_id") : null);
                        option.setRequiredPsychologicalState(requiredStateStr);

                        optionsList.add(option);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return optionsList;
    }


    public static Option getOptionById(int optionId) {
        Option option = null;
        String query = "SELECT * FROM options WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, optionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                option = new Option();
                option.setId(rs.getInt("id"));
                option.setDecisionId(rs.getInt("decision_id"));
                option.setText(rs.getString("text"));
                option.setPointsM(rs.getInt("points_m"));
                option.setPointsR(rs.getInt("points_r"));
                option.setNextDecisionId(rs.getObject("next_decision_id") != null ? rs.getInt("next_decision_id") : null);
                option.setRequiredPsychologicalState(rs.getString("required_psychological_state"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return option;
    }


    public static boolean createOption(Option option) {
        String query = "INSERT INTO options (decision_id, text, points_m, points_r, next_decision_id, required_psychological_state) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, option.getDecisionId());
            stmt.setString(2, option.getText());
            stmt.setInt(3, option.getPointsM());
            stmt.setInt(4, option.getPointsR());

            if (option.getNextDecisionId() != null) {
                stmt.setInt(5, option.getNextDecisionId());
            } else {
                stmt.setNull(5, com.us.TFG.db.Types.INTEGER);
            }

            stmt.setString(6, option.getRequiredPsychologicalState());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean updateOption(Option option) {
        String query = "UPDATE options SET text = ?, points_m = ?, points_r = ?, next_decision_id = ?, required_psychological_state = ? WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, option.getText());
            stmt.setInt(2, option.getPointsM());
            stmt.setInt(3, option.getPointsR());

            if (option.getNextDecisionId() != null) {
                stmt.setInt(4, option.getNextDecisionId());
            } else {
                stmt.setNull(4, com.us.TFG.db.Types.INTEGER);
            }

            stmt.setString(5, option.getRequiredPsychologicalState());
            stmt.setInt(6, option.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean deleteOption(int optionId) {
        String query = "DELETE FROM options WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, optionId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
