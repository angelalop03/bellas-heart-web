package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Game;
import com.us.TFG.model.GameState;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class GameManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_GameManagerTest.db";

    @Before
    public void setup() {
        try {
            File original = new File(ORIGINAL_DB_PATH);
            File testCopy = new File(TEST_DB_PATH);

            if (testCopy.exists()) {
                if (!testCopy.delete()) {
                    throw new IOException("No se pudo eliminar game_test.db");
                }
            }

            Files.copy(original.toPath(), testCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
    public void testGetGameById() {
        Game game = GameManager.getGameById(1);
        assertNotNull("Debe devolver una partida válida con ID 1", game);
        assertEquals(1, game.getId());
    }

    @Test
    public void testCreateGame() {
        Game game = GameManager.createGame("Partida Test", 1, 1);
        assertNotNull("El juego creado no debe ser null", game);
        assertTrue("El ID del juego debe ser mayor que 0", game.getId() > 0);
    }

    @Test
    public void testGetGamesForPlayer() {
        List<Game> games = GameManager.getGamesForPlayer(1);
        assertNotNull(games);
        assertFalse("El jugador 1 debe tener al menos una partida", games.isEmpty());
    }

    @Test
    public void testDoesGameExist() {
        boolean exists = GameManager.doesGameExist("Partida1", 1);
        assertTrue("La partida debería existir para el jugador 1", exists);

        boolean notExists = GameManager.doesGameExist("NoExiste", 999);
        assertFalse("Esta partida no debería existir", notExists);
    }

    @Test
    public void testUpdatePoints() {
        Game before = GameManager.getGameById(1);
        GameManager.updatePoints(1, 5, -2);
        Game after = GameManager.getGameById(1);

        assertEquals(before.getPointsM() + 5, after.getPointsM());
        assertEquals(before.getPointsR() - 2, after.getPointsR());
    }

    @Test
    public void testCreateGameWithDuplicateName() {
        String name = "Duplicado Test";
        Game game1 = GameManager.createGame(name, 1, 1);
        assertNotNull(game1);

        boolean exists = GameManager.doesGameExist(name, 1);
        assertTrue("El nombre duplicado debería detectarse", exists);
    }

    @Test
    public void testGetGameByIdInvalid() {
        Game game = GameManager.getGameById(-999);
        assertNull("No debe devolver un juego para un ID inválido", game);
    }

    @Test
    public void testUpdatePointsWithInvalidGameId() {
        GameManager.updatePoints(-1, 10, 10);
    }


}
