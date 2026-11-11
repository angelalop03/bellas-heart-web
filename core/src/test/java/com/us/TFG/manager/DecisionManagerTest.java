package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Decision;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class DecisionManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_DecisionManagerTest.db";

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
    public void testGetFirstDecisionId() {
        int id = DecisionManager.getFirstDecisionId(1);
        assertTrue(id > 0);
    }

    @Test
    public void testUpdateCurrentDecision() {
        DecisionManager.updateCurrentDecision(1, 2);
        int decisionId = GameManager.getGameById(1).getCurrentDecisionId();
        assertEquals(2, decisionId);
    }



    @Test
    public void testGetDecisionsBySceneId() {
        List<Decision> decisions = DecisionManager.getDecisionsBySceneId(1);
        assertNotNull(decisions);
        assertFalse(decisions.isEmpty());
    }

    @Test
    public void testCreateAndDeleteDecision() {
        Decision decision = new Decision();
        decision.setSceneId(1);
        decision.setDecisionOrder(99);
        decision.setCharacterId(1);
        decision.setDialogue("¿Continuamos?");

        int id = DecisionManager.createDecision(decision);
        assertTrue(id > 0);

        boolean deleted = DecisionManager.deleteDecision(id);
        assertTrue(deleted);
    }

    @Test
    public void testUpdateDecision() {
        Decision decision = DecisionManager.getDecisionById(1);
        assertNotNull(decision);

        String original = decision.getDialogue();
        decision.setDialogue("Cambio temporal");
        boolean updated = DecisionManager.updateDecision(decision);
        assertTrue(updated);

        decision.setDialogue(original);
        DecisionManager.updateDecision(decision);
    }

    @Test
    public void testGetFirstDecisionIdReturnsMinusOneForInvalidScene() {
        int id = DecisionManager.getFirstDecisionId(-999);
        assertEquals(-1, id);
    }

}
