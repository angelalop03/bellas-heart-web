package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Scene;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class SceneManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_SceneManagerTest.db";

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
    public void testGetSceneById() {
        Scene scene = SceneManager.getSceneById(1);
        assertNotNull("Debe devolver una escena válida para el ID 1", scene);
    }

    @Test
    public void testGetFirstSceneId() {
        int sceneId = SceneManager.getFirstSceneId(1);
        assertTrue("Debe devolver un ID de escena válido", sceneId > 0);
    }

    @Test
    public void testGetNextSceneInChapter() {
        Scene nextScene = SceneManager.getNextSceneInChapter(1, 1);
        assertNotNull("Debe devolver la siguiente escena si existe", nextScene);
    }

    @Test
    public void testUpdateCurrentScene() {
        SceneManager.updateCurrentScene(1, 1); // No excepción = éxito
    }

    @Test
    public void testGetFirstSceneOfChapter() {
        Scene scene = SceneManager.getFirstSceneOfChapter(1);
        assertNotNull("Debe devolver la primera escena del capítulo", scene);
    }

    @Test
    public void testGetScenesByChapterId() {
        List<Scene> scenes = SceneManager.getScenesByChapterId(1);
        assertNotNull("Debe devolver una lista de escenas", scenes);
        assertFalse("La lista de escenas no debe estar vacía", scenes.isEmpty());
    }

    @Test
    public void testCreateUpdateDeleteScene() {
        Scene scene = new Scene(0, 1, "test_bg.png", 999);
        int newId = SceneManager.createScene(scene);
        assertTrue("Debe devolver un ID válido", newId > 0);

        scene.setId(newId);
        scene.setBackgroundImage("updated_bg.png");
        boolean updated = SceneManager.updateScene(scene);
        assertTrue("Debe actualizar correctamente la escena", updated);

        boolean deleted = SceneManager.deleteScene(newId);
        assertTrue("Debe eliminar correctamente la escena", deleted);
    }
}
