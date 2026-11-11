package com.us.TFG.screens.General;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.UserManager;
import com.us.TFG.model.Authorities;
import com.us.TFG.screens.Admin.AdminMenuScreen;
import com.us.TFG.screens.Player.PlayerMenuScreen;
import com.us.TFG.utils.Config;
import com.us.TFG.utils.Render;
import com.us.TFG.utils.Resources;
import com.us.TFG.utils.UIStyle;

public class LoginScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private BitmapFont font;
    private SpriteBatch batch;
    private Imagen fondo;
    private Label errorLabel;

    @Override
    public void show() {
        // Configurar Stage y Skin
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin();

        // Cargar el fondo
        fondo = new Imagen(Resources.MENU);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        font = UIStyle.getFont();
        batch = Render.batch;


        // Campos de entrada de usuario y contraseña
        TextField usernameField = new TextField("", UIStyle.getTextFieldStyle());
        usernameField.setMessageText("Nombre de usuario");
        usernameField.setPosition(Gdx.graphics.getWidth() / 2f - 150, 300);
        usernameField.setSize(300, 60);
        stage.addActor(usernameField);

        TextField passwordField = new TextField("",  UIStyle.getTextFieldStyle());
        passwordField.setMessageText("Contraseña");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        passwordField.setPosition(Gdx.graphics.getWidth() / 2f - 150, 220);
        passwordField.setSize(300, 60);
        stage.addActor(passwordField);

        // Label para mensaje de error
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.RED;
        errorLabel = new Label("Usuario o contraseña incorrectos.", labelStyle);
        errorLabel.setPosition(Gdx.graphics.getWidth() / 2f - 200, 180);
        errorLabel.setVisible(false);
        stage.addActor(errorLabel);

        // Botón de inicio de sesión
        TextButton loginButton = new TextButton("Iniciar Sesión",  UIStyle.getButtonStyle());
        loginButton.setSize(300, 60);
        loginButton.setPosition(Gdx.graphics.getWidth() / 2f - 150, 120);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText();
                String password = passwordField.getText();

                // Verificar credenciales usando la instancia global de DatabaseManager
                if (UserManager.verifyLogin(username, password)) {
                    Authorities authority = UserManager.getAuthority(username);
                    if (authority == Authorities.ADMIN) {
                        Render.app.setScreen(new AdminMenuScreen(username));
                    } else if (authority == Authorities.PLAYER) {
                        Render.app.setScreen(new PlayerMenuScreen(username));
                    }
                } else {
                    errorLabel.setVisible(true);  // Mostrar el mensaje de error
                }
            }
        });
        stage.addActor(loginButton);

        // Botón de regresar
        TextButton backButton = new TextButton("Volver", UIStyle.getButtonStyle());
        backButton.setSize(300, 60);
        backButton.setPosition(Gdx.graphics.getWidth() / 2f - 150, 40);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new OpenScreen());
            }
        });
        stage.addActor(backButton);
    }

    @Override
    public void render(float delta) {
        batch.begin();
        fondo.draw();
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }

}
