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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.UserManager;
import com.us.TFG.screens.Player.PlayerMenuScreen;
import com.us.TFG.utils.Config;
import com.us.TFG.utils.Render;
import com.us.TFG.utils.Resources;
import com.us.TFG.utils.UIStyle;
import com.us.TFG.utils.validators.InputValidator;

public class RegisterScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private TextField usernameField, passwordField, firstnameField, lastnameField, emailField;
    private Label feedbackLabel;
    private Imagen fondo;
    private SpriteBatch batch;
    private BitmapFont font;

    @Override
    public void show() {
        fondo = new Imagen(Resources.MENU);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        font = UIStyle.getFont();

        // Crear campos de entrada
        usernameField = new TextField("", UIStyle.getTextFieldStyle());
        usernameField.setMessageText("Nombre de usuario");
        passwordField = new TextField("", UIStyle.getTextFieldStyle());
        passwordField.setMessageText("Contraseña");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        firstnameField = new TextField("", UIStyle.getTextFieldStyle());
        firstnameField.setMessageText("Nombre");
        lastnameField = new TextField("", UIStyle.getTextFieldStyle());
        lastnameField.setMessageText("Apellido");
        emailField = new TextField("", UIStyle.getTextFieldStyle());
        emailField.setMessageText("Correo electrónico");

        // Botón de registro
        TextButton registerButton = new TextButton("Registrar", UIStyle.getButtonStyle());
        registerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String firstname = firstnameField.getText().trim();
                String lastname = lastnameField.getText().trim();
                String email = emailField.getText().trim();

                String validationMessage = InputValidator.validateInputs2(username, password, firstname, lastname, email);
                if (validationMessage != null) {
                    UIStyle.showError(feedbackLabel,validationMessage);
                    return;
                }

                if (UserManager.registerUser(username, password, firstname, lastname, email)) {
                    Render.app.setScreen(new PlayerMenuScreen(username));
                } else if (UserManager.usernameExists(username)){
                    UIStyle.showError(feedbackLabel,"Nombre de usuario no disponible ");
                }
                else {
                    UIStyle.showError(feedbackLabel,"Error al crear usuario");
                }
            }
        });

        // Botón de regresar
        TextButton backButton = new TextButton("Volver", UIStyle.getButtonStyle());
        backButton.setSize(300, 40);
        backButton.setPosition(Gdx.graphics.getWidth() / 2f - 150, 60);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new OpenScreen());
            }
        });
        stage.addActor(backButton);

        // Etiqueta de feedback para mostrar mensajes
        feedbackLabel = new Label("", UIStyle.getLabelStyle());

        // Organizar los elementos en una tabla
        Table table = new Table();
        table.setFillParent(true);
        table.add(new Label("Registrar Nuevo Usuario", UIStyle.getLabelStyle())).colspan(2).padBottom(30);
        table.row();
        table.add(usernameField).width(400).padBottom(15);
        table.row();
        table.add(passwordField).width(400).padBottom(15);
        table.row();
        table.add(firstnameField).width(400).padBottom(15);
        table.row();
        table.add(lastnameField).width(400).padBottom(15);
        table.row();
        table.add(emailField).width(400).padBottom(15);
        table.row();
        table.add(registerButton).width(300).padTop(20).padBottom(20);
        table.row();
        table.add(feedbackLabel).colspan(2);
        stage.addActor(table);
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
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }







}
