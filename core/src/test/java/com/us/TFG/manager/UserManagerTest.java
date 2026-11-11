package com.us.TFG.manager;

import com.us.TFG.MyTfgGame;
import com.us.TFG.model.Authorities;
import com.us.TFG.utils.DatabaseManager;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.*;

public class UserManagerTest {

    private static final String ORIGINAL_DB_PATH = "src/test/resources/game.db";
    private static final String TEST_DB_PATH = "game_test_UserManagerTest.db";

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
    public void testRegisterUserSuccess() {
        boolean result = UserManager.registerUser("new_user", "password123", "Test", "User", "test@example.com");
        assertTrue("El usuario debería registrarse correctamente", result);
        assertTrue(UserManager.usernameExists("new_user"));
    }

    @Test
    public void testRegisterUserDuplicate() {
        UserManager.registerUser("duplicate_user", "pass", "Dup", "User", "dup@example.com");
        boolean result = UserManager.registerUser("duplicate_user", "pass", "Dup", "User", "dup@example.com");
        assertFalse("No debería permitir registrar un usuario duplicado", result);
    }

    @Test
    public void testUsernameExists() {
        UserManager.registerUser("exists_user", "123", "Name", "Last", "mail@mail.com");
        assertTrue("El nombre de usuario debería existir", UserManager.usernameExists("exists_user"));
        assertFalse("Un nombre de usuario inexistente no debería existir", UserManager.usernameExists("no_user"));
    }

    @Test
    public void testVerifyLogin() {
        UserManager.registerUser("login_test", "securepass", "Login", "User", "log@test.com");
        assertTrue("La verificación debería ser exitosa con la contraseña correcta", UserManager.verifyLogin("login_test", "securepass"));
        assertFalse("La verificación debería fallar con una contraseña incorrecta", UserManager.verifyLogin("login_test", "wrongpass"));
        assertFalse("La verificación debería fallar con un usuario inexistente", UserManager.verifyLogin("nouser", "1234"));
    }


    @Test
    public void testGetAuthority() {
        UserManager.registerUser("auth_user", "authpass", "Auth", "User", "auth@user.com");
        Authorities auth = UserManager.getAuthority("auth_user");
        assertEquals("La autoridad por defecto debería ser USER (2)", Authorities.PLAYER, auth);
    }

    @Test
    public void testUpdatePassword() {
        UserManager.registerUser("pass_user", "oldpass", "Pass", "User", "pass@user.com");
        boolean updated = UserManager.updatePassword("pass_user", "newpass");
        assertTrue("La contraseña debería actualizarse correctamente", updated);
        assertTrue("La nueva contraseña debería verificarse correctamente", UserManager.verifyLogin("pass_user", "newpass"));
    }

    @Test
    public void testUpdateProfile() {
        UserManager.registerUser("profile_user", "pass", "First", "Last", "mail@mail.com");
        boolean updated = UserManager.updateProfile("profile_user", "profile_user_updated", "NewFirst", "NewLast", "new@mail.com", null);
        assertTrue("El perfil debería actualizarse correctamente", updated);
    }

    @Test
    public void testDeleteUser() {
        UserManager.registerUser("delete_me", "pass", "Del", "User", "del@me.com");
        boolean deleted = UserManager.deleteUser("delete_me");
        assertTrue("El usuario debería eliminarse correctamente", deleted);
        assertFalse("El usuario ya no debería existir", UserManager.usernameExists("delete_me"));
    }
}
