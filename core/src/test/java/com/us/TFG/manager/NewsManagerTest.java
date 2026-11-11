package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.manager.NewsManager.NewsData;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class NewsManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_NewsManagerTest.db";

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
            throw new RuntimeException("Error al preparar la base de datos de test", e);
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
    public void testCreateNews() {
        boolean created = NewsManager.createNews("Noticia de prueba", "Este es el contenido de la noticia");
        assertTrue("Debe crearse la noticia correctamente", created);
    }

    @Test
    public void testGetAllNews() {
        NewsManager.createNews("Noticia Visible", "Contenido visible");
        List<NewsData> newsList = NewsManager.getAllNews();
        assertNotNull(newsList);
        assertTrue("Debe haber al menos una noticia", newsList.size() > 0);
    }

    @Test
    public void testUpdateNews() {
        NewsManager.createNews("Actualizar", "Texto viejo");
        List<NewsData> newsList = NewsManager.getAllNews();
        int id = newsList.get(newsList.size() - 1).getId();
        boolean updated = NewsManager.updateNews(id, "Actualizado", "Texto nuevo");
        assertTrue("Debe actualizar correctamente", updated);
    }

    @Test
    public void testDeleteNews() {
        NewsManager.createNews("Eliminar esto", "Texto que se eliminará");
        List<NewsData> newsList = NewsManager.getAllNews();
        int id = newsList.get(newsList.size() - 1).getId();
        boolean deleted = NewsManager.deleteNews(id);
        assertTrue("Debe eliminar la noticia correctamente", deleted);
    }

    @Test
    public void testCreateNewsWithNullValues() {
        boolean result = NewsManager.createNews(null, null);
        assertFalse("Crear noticia con valores nulos debería fallar", result);
    }

    @Test
    public void testDeleteNewsWithInvalidId() {
        boolean deleted = NewsManager.deleteNews(-999);
        assertFalse("Eliminar noticia con ID inválido debería devolver false", deleted);
    }

    @Test
    public void testUpdateNewsWithInvalidId() {
        boolean updated = NewsManager.updateNews(-1, "Título inválido", "Texto inválido");
        assertFalse("Actualizar noticia con ID inválido debería devolver false", updated);
    }

    @Test
    public void testUpdateNewsWithNullFields() {

        boolean updated = NewsManager.updateNews(1, null, null);
        assertFalse("Actualizar noticia con campos nulos debería devolver false", updated);
    }

}
