package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Game;
import com.us.TFG.model.Option;
import com.us.TFG.model.PsychologicalState;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.*;

public class OptionManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_OptionManagerTest.db";

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
    public void testGetOptionsByDecisionId() {
        List<Option> options = OptionManager.getOptionsByDecisionId(1);
        assertNotNull(options);
    }



    @Test
    public void testGetOptionById() {
        Option option = OptionManager.getOptionById(1);
        assertNotNull("Debe devolver una opción válida para ID 1", option);
        assertEquals(1, option.getId());
    }

    @Test
    public void testCreateOption() {
        Option option = new Option();
        option.setDecisionId(1);
        option.setText("Opción de prueba");
        option.setPointsM(5);
        option.setPointsR(-3);
        option.setNextDecisionId(null);
        option.setRequiredPsychologicalState(null);

        boolean result = OptionManager.createOption(option);
        assertTrue("La opción debería crearse correctamente", result);
    }

    @Test
    public void testUpdateOption() {
        Option option = OptionManager.getOptionById(1);
        assertNotNull("Debe existir una opción con ID 1 para actualizar", option);

        String newText = "Texto actualizado";
        option.setText(newText);
        option.setPointsM(option.getPointsM() + 1);
        option.setPointsR(option.getPointsR() - 1);

        boolean updated = OptionManager.updateOption(option);
        assertTrue("La opción debe actualizarse correctamente", updated);

        Option updatedOption = OptionManager.getOptionById(option.getId());
        assertEquals("El texto debe coincidir con el actualizado", newText, updatedOption.getText());
    }

    @Test
    public void testDeleteOption() {
        Option option = new Option();
        option.setDecisionId(1);
        option.setText("Opción para eliminar");
        option.setPointsM(0);
        option.setPointsR(0);
        option.setNextDecisionId(null);
        option.setRequiredPsychologicalState(null);

        boolean created = OptionManager.createOption(option);
        assertTrue("Debe crearse la opción para luego eliminarla", created);

        List<Option> options = OptionManager.getOptionsByDecisionId(1);
        int lastOptionId = options.get(options.size() - 1).getId();

        boolean deleted = OptionManager.deleteOption(lastOptionId);
        assertTrue("La opción debería ser eliminada correctamente", deleted);
    }



}
