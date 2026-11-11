package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    public static String getCurrentProfileImage(String currentUsername) {
        String query = "SELECT image FROM players WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        String currentImage = "default_image.png"; // Imagen predeterminada

        if (connection == null) {
            return currentImage;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentImage = rs.getString("image");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return currentImage;
    }

    public static String getUsername(int playerId) {
        String query = "SELECT u.username " +
            "FROM users u " +
            "JOIN players p ON u.id = p.user_id " +
            "WHERE p.id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        String username = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                username = rs.getString("username");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return username;
    }

    public static String getPassword(int playerId) {
        String query = "SELECT u.password " +
            "FROM users u " +
            "JOIN players p ON u.id = p.user_id " +
            "WHERE p.id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        String password = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                password = rs.getString("password");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return password;
    }


    public static PlayerData getPlayerData(String username) {
        String query = "SELECT p.id, p.firstname, p.lastname, p.email, p.image " +
            "FROM players p JOIN users u ON p.user_id = u.id " +
            "WHERE u.username = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new PlayerData(
                    resultSet.getInt("id"), // Incluye el id
                    resultSet.getString("firstname"),
                    resultSet.getString("lastname"),
                    resultSet.getString("email"),
                    resultSet.getString("image")
                );
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Si no se encuentra el jugador
    }

    public static List<PlayerData> getAllPlayers() {
        String query = "SELECT id, firstname, lastname, email, image FROM players";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        List<PlayerData> players = new ArrayList<>();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                players.add(new PlayerData(
                    rs.getInt("id"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("image")
                ));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }


    // Clase auxiliar para encapsular los datos del jugador
    public static class PlayerData {
        private final int id;
        private final String firstname;
        private final String lastname;
        private final String email;
        private final String image;

        public PlayerData(int id, String firstname, String lastname, String email, String image) {
            this.id = id;
            this.firstname = firstname;
            this.lastname = lastname;
            this.email = email;
            this.image = image;
        }

        public int getId() { // Nuevo getter para id
            return id;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public String getEmail() {
            return email;
        }

        public String getImage() {
            return image;
        }
    }

}
