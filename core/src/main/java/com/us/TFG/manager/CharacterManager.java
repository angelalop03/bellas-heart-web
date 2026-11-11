package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;

import com.us.TFG.db.Connection;
import com.us.TFG.db.PreparedStatement;
import com.us.TFG.db.ResultSet;
import com.us.TFG.db.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.us.TFG.model.CharacterGame;


public class CharacterManager {

    public static CharacterGame getCharacterById(int characterId) {
        String query = "SELECT * FROM characters WHERE id = ?";
        Connection connection = MyTfgGame.databaseManager.getConnection();
        CharacterGame character = null;

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, characterId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                character = new CharacterGame(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image_path")
                );
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return character;
    }

    public static List<CharacterGame> getAllCharacters() {
        List<CharacterGame> characters = new ArrayList<>();
        String query = "SELECT * FROM characters";
        Connection connection = MyTfgGame.databaseManager.getConnection();

        if (connection == null) {
            throw new RuntimeException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CharacterGame character = new CharacterGame(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image_path")
                );
                characters.add(character);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return characters;
    }



}
