package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Chapter;
import com.us.TFG.model.Game;
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

public class ChapterManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_ChapterManagerTest.db";

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
    public void testGetIntroChapterForInvalidHistoryReturnsNull() {
        Chapter chapter = ChapterManager.getIntroChapterForHistory(-999);
        assertNull(chapter);
    }

    @Test
    public void testFindFirstChapterReturnsMinusOneForInvalidHistoryId() {
        int firstChapterId = ChapterManager.findFirstChapter(-999);
        assertEquals(-1, firstChapterId);
    }

    @Test
    public void testGetChaptersByInvalidHistoryReturnsEmptyList() {
        List<Chapter> chapters = ChapterManager.getChaptersByHistoryId(-999);
        assertTrue(chapters.isEmpty());
    }

    @Test
    public void testGetChapterByInvalidIdReturnsNull() {
        Chapter chapter = ChapterManager.getChapterById(-999);
        assertNull(chapter);
    }

    @Test
    public void testGetChaptersForPlayerAndGame() {
        List<Chapter> chapters = ChapterManager.getChaptersForPlayerandGame(1, 1);
        assertNotNull(chapters);
        assertFalse("La lista de capítulos desbloqueados no debería estar vacía.",chapters.isEmpty());
    }

    @Test
    public void testIsChapterPlayed() {
        // Este capítulo existe pero aún no está jugado
        boolean played = ChapterManager.isChapterPlayed(1, 2);
        assertFalse( "El capítulo 2 para el juego 1 no debería estar marcado como jugado.",played);
    }

    @Test
    public void testFindFirstChapter() {
        int chapterId = ChapterManager.findFirstChapter(1);
        assertTrue("Debe devolver el ID del primer capítulo de la historia 1.",chapterId > 0);
    }

    @Test
    public void testGetChapterById() {
        Chapter chapter = ChapterManager.getChapterById(2); // "Introducción"
        assertNotNull(chapter);
        assertEquals("Introducción", chapter.getName());
        assertEquals("0", chapter.getChapterNumber());
    }

    @Test
    public void testGetChaptersByHistoryId() {
        List<Chapter> chapters = ChapterManager.getChaptersByHistoryId(1);
        assertNotNull(chapters);
        assertTrue( "Debería haber varios capítulos para la historia 1.",chapters.size() > 5);
    }

    @Test
    public void testGetIntroChapterForHistory() {
        Chapter intro = ChapterManager.getIntroChapterForHistory(1);
        assertNotNull(intro);
        assertEquals("0", intro.getChapterNumber());
    }

    @Test
    public void testUnlockNextChapter() {
        // gameId = 1, currentChapterId = 3 (chapter_number '1')
        int nextId = ChapterManager.unlockNextChapter(1, 3);
        assertTrue( "Debería desbloquear el siguiente capítulo para la partida 1.",nextId > 0);
    }

    @Test
    public void testMarkChapterAsPlayed() {
        ChapterManager.markChapterAsPlayed(1, 3); // marcar como jugado
        assertTrue(ChapterManager.isChapterPlayed(1, 3));
    }

    @Test
    public void testCreateChapter() {
        Chapter chapter = new Chapter();
        chapter.setName("Test Chapter");
        chapter.setChapterNumber("99");
        chapter.setImagePath("test.png");
        chapter.setRequiredRelationshipState(null);
        chapter.setHistory(1);

        boolean created = ChapterManager.createChapter(chapter);
        assertTrue("El capítulo debería crearse correctamente", created);

        // Verificar que existe al menos uno con ese nombre
        List<Chapter> chapters = ChapterManager.getChaptersByHistoryId(1);
        boolean found = chapters.stream().anyMatch(c -> "Test Chapter".equals(c.getName()));
        assertTrue("El capítulo creado debería estar en la base de datos", found);
    }

    @Test
    public void testUpdateChapter() {
        Chapter chapter = ChapterManager.getChapterById(2);
        assertNotNull(chapter);

        String originalName = chapter.getName();
        chapter.setName("Updated Chapter");
        chapter.setChapterNumber("0.5");

        boolean updated = ChapterManager.updateChapter(chapter);
        assertTrue("El capítulo debería actualizarse correctamente", updated);

        Chapter updatedChapter = ChapterManager.getChapterById(2);
        assertEquals("Updated Chapter", updatedChapter.getName());

        // Restaurar el nombre original por si otros tests dependen de él
        chapter.setName(originalName);
        ChapterManager.updateChapter(chapter);
    }

    @Test
    public void testDeleteChapter() {
        // Crear uno temporal
        Chapter chapter = new Chapter();
        chapter.setName("Temp Chapter");
        chapter.setChapterNumber("77");
        chapter.setImagePath("temp.png");
        chapter.setRequiredRelationshipState(null);
        chapter.setHistory(1);

        boolean created = ChapterManager.createChapter(chapter);
        assertTrue(created);

        // Obtener el ID del nuevo capítulo
        List<Chapter> chapters = ChapterManager.getChaptersByHistoryId(1);
        Chapter last = chapters.get(chapters.size() - 1);
        int tempId = last.getId();

        ChapterManager.deleteChapter(tempId);
        Chapter deleted = ChapterManager.getChapterById(tempId);
        assertNull("El capítulo debería haber sido eliminado", deleted);
    }

    @Test
    public void testUpdateCurrentChapter() {
        ChapterManager.updateCurrentChapter(1, 4); // gameId = 1

        Game updatedGame = GameManager.getGameById(1);
        assertEquals("El capítulo actual debería haberse actualizado a 4", 4, updatedGame.getCurrentChapterId());
    }

    @Test
    public void testUnlockFirstChapter() {
        Game game = GameManager.getGameById(2); // usa una partida distinta a la 1

        // Forzamos chapter_id a otro valor para verificar que lo actualiza
        ChapterManager.updateCurrentChapter(game.getId(), 2);

        ChapterManager.unlockFirstChapter(1, game, 1);
        int firstChapterId = ChapterManager.findFirstChapter(1);

        Game updatedGame = GameManager.getGameById(game.getId());
        assertEquals("Debería actualizarse el currentChapterId al primer capítulo", firstChapterId, updatedGame.getCurrentChapterId());
    }

}
