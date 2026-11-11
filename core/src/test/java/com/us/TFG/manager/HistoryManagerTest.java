package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.History;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class HistoryManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_HistoryManagerTest.db";

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
    public void testCreateAndFetchHistory() {
        History history = new History();
        history.setTitle("Test Historia");
        history.setPublished(true);
        history.setImagePath("path/test.jpg");
        history.setDescription("Descripción de prueba");

        boolean success = HistoryManager.createHistory(history);
        assertTrue("La historia debe crearse exitosamente", success);

        List<History> all = HistoryManager.getHistories();
        assertTrue("Debe haber al menos una historia", all.size() > 0);
    }

    @Test
    public void testUpdateHistory() {
        HistoryManager.updateHistory(1, "Nuevo título", "Nueva descripción");
        List<History> all = HistoryManager.getHistories();
        boolean encontrado = all.stream().anyMatch(h -> "Nuevo título".equals(h.getTitle()));
        assertTrue("Debe haberse actualizado el título", encontrado);
    }

    @Test
    public void testGetHistoriesPublished() {
        List<History> published = HistoryManager.getHistoriesPublished();
        assertNotNull(published);
        for (History h : published) {
            assertTrue("Todas deben estar publicadas", h.isPublished());
        }
    }

    @Test
    public void testSetPublished() {
        HistoryManager.setPublished(1, false);
        List<History> published = HistoryManager.getHistoriesPublished();
        boolean stillPublished = published.stream().anyMatch(h -> h.getId() == 1);
        assertFalse("La historia debería haber sido despublicada", stillPublished);
    }

    @Test
    public void testGetHistoryByChapterId() {
        int chapterId = 3;
        int expectedHistoryId = 1;

        History history = HistoryManager.getHistoryByChapterId(chapterId);

        assertEquals("El ID de la historia debe coincidir con el esperado", expectedHistoryId, history.getId());
    }


    @Test
    public void testDeleteHistory() {
        History history = new History();
        history.setTitle("Historia a borrar");
        history.setPublished(false);
        history.setImagePath("del.jpg");
        history.setDescription("desc");

        boolean created = HistoryManager.createHistory(history);
        assertTrue(created);

        List<History> all = HistoryManager.getHistories();
        int lastId = all.get(all.size() - 1).getId();
        HistoryManager.deleteHistory(lastId);

        List<History> postDelete = HistoryManager.getHistories();
        boolean exists = postDelete.stream().anyMatch(h -> h.getId() == lastId);
        assertFalse("La historia debería haber sido eliminada", exists);
    }

    @Test
    public void testCreateValidHistory() {
        History history = new History();
        history.setTitle("Historia válida");
        history.setPublished(true);
        history.setImagePath("imagenes/historia_valida.png");
        history.setDescription("Una historia de prueba válida para insertar en la base de datos.");

        boolean result = HistoryManager.createHistory(history);
        assertTrue("La historia válida debería insertarse correctamente", result);

        // Verificar que fue insertada
        List<History> allHistories = HistoryManager.getHistories();
        boolean found = allHistories.stream().anyMatch(h ->
            "Historia válida".equals(h.getTitle()) &&
                "imagenes/historia_valida.png".equals(h.getImagePath()) &&
                h.isPublished() &&
                "Una historia de prueba válida para insertar en la base de datos.".equals(h.getDescription())
        );

        assertTrue("La historia recién insertada debería estar en la base de datos", found);
    }



}
