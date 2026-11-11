package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import com.us.TFG.db.*;

import static org.junit.Assert.*;

public class PlayerChapterStateManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_PlayerChapterStateManagerTest.db";

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
    public void testUnlockChapterForPlayer() throws SQLException {
        int playerId = 1;
        int gameId = 1;
        int chapterId = 2;

        PlayerChapterStateManager.unlockChapterForPlayer(playerId, gameId, chapterId);

        // Verificamos si se insertó correctamente
        Connection conn = MyTfgGame.databaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM player_chapter_state WHERE player_id = ? AND game_id = ? AND chapter_id = ?");
        stmt.setInt(1, playerId);
        stmt.setInt(2, gameId);
        stmt.setInt(3, chapterId);

        ResultSet rs = stmt.executeQuery();
        assertTrue("Debe haber un registro en player_chapter_state", rs.next());
        assertTrue("El capítulo debe estar desbloqueado", rs.getBoolean("unlocked"));
        assertFalse("El capítulo aún no debe estar jugado", rs.getBoolean("played"));

        rs.close();
        stmt.close();
    }
}
