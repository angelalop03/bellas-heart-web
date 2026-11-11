package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.CharacterGame;
import com.us.TFG.utils.DatabaseManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class CharacterManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_CharacterManagerTest.db";

    @Before
    public void setup() {
        try {
            File original = new File(ORIGINAL_DB_PATH);
            File testCopy = new File(TEST_DB_PATH);

            // Eliminar si ya existe
            if (testCopy.exists()) {
                if (!testCopy.delete()) {
                    throw new IOException("No se pudo eliminar game_test.db");
                }
            }

            // Copiar base de datos original
            Files.copy(original.toPath(), testCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Inicializar y conectar
            MyTfgGame.databaseManager = new DatabaseManager();
            MyTfgGame.databaseManager.connect("jdbc:sqlite:" + TEST_DB_PATH);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo copiar game.db a game_test.db", e);
        }
    }

    @After
    public void cleanup() {
        try {
            if (MyTfgGame.databaseManager != null) {
                MyTfgGame.databaseManager.close();
                MyTfgGame.databaseManager = null; // Limpieza explícita
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar la base de datos:");
            e.printStackTrace();
        }

        File testDbFile = new File(TEST_DB_PATH);
        if (testDbFile.exists()) {
            if (!testDbFile.delete()) {
                throw new RuntimeException("No se pudo eliminar " + TEST_DB_PATH + ". Puede estar en uso o bloqueado.");
            }
        }
    }


    @Test
    public void testGetCharacterByValidId() {
        CharacterGame character = CharacterManager.getCharacterById(1);
        assertNotNull("Debería devolver un personaje válido", character);
        assertEquals("Nombre del personaje esperado", "David", character.getName());
    }

    @Test
    public void testGetCharacterByInvalidIdReturnsNull() {
        CharacterGame character = CharacterManager.getCharacterById(-999);
        assertNull("No debería devolver un personaje para ID inválido", character);
    }

    @Test
    public void testGetAllCharacters() {
        List<CharacterGame> characters = CharacterManager.getAllCharacters();
        assertNotNull("La lista de personajes no debería ser null", characters);
        assertFalse("Debería haber al menos un personaje", characters.isEmpty());
    }

}
