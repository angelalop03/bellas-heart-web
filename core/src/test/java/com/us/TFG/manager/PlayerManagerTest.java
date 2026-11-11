package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.manager.PlayerManager.PlayerData;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class PlayerManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_PlayerManagerTest.db";

    @Before
    public void setup() {
        try {
            File original = new File(ORIGINAL_DB_PATH);
            File testCopy = new File(TEST_DB_PATH);

            if (testCopy.exists()) testCopy.delete();
            Files.copy(original.toPath(), testCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            MyTfgGame.databaseManager = new DatabaseManager();
            MyTfgGame.databaseManager.connect("jdbc:sqlite:" + TEST_DB_PATH);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo preparar la base de datos para test", e);
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
    public void testGetCurrentProfileImage() {
        String image = PlayerManager.getCurrentProfileImage("testuser");
        assertNotNull("Debe devolver una imagen, incluso si es la predeterminada", image);
    }

    @Test
    public void testGetCurrentProfileImage_NotFound() {
        String image = PlayerManager.getCurrentProfileImage("usuario_que_no_existe");
        assertEquals("Debe devolver la imagen predeterminada si no se encuentra el usuario", "default_image.png", image);
    }

    @Test
    public void testGetUsername() {
        String username = PlayerManager.getUsername(1);
        assertNotNull("Debe devolver un nombre de usuario válido para el ID", username);
    }

    @Test
    public void testGetUsername_NotFound() {
        String username = PlayerManager.getUsername(-999); // ID inválido
        assertNull("Debe devolver null si no se encuentra el jugador", username);
    }

    @Test
    public void testGetPassword() {
        String password = PlayerManager.getPassword(1);
        assertNotNull("Debe devolver la contraseña asociada al jugador", password);
    }

    @Test
    public void testGetPassword_NotFound() {
        String password = PlayerManager.getPassword(-999);
        assertNull("Debe devolver null si no se encuentra el jugador", password);
    }

    @Test
    public void testGetPlayerData_NotFound() {
        PlayerData data = PlayerManager.getPlayerData("usuario_que_no_existe");
        assertNull("Debe devolver null si no se encuentra el jugador", data);
    }

    @Test
    public void testGetAllPlayers() {
        List<PlayerData> players = PlayerManager.getAllPlayers();
        assertNotNull("La lista no debe ser nula", players);
        assertFalse("Debe haber al menos un jugador en la base de datos", players.isEmpty());
    }
}
