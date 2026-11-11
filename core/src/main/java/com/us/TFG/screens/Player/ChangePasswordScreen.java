package com.us.TFG.screens.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.model.Authorities;
import com.us.TFG.screens.Admin.AdminPerfilScreen;
import com.us.TFG.utils.*;
import com.us.TFG.manager.UserManager;
import com.us.TFG.utils.validators.InputValidator;

public class ChangePasswordScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private TextField newPasswordField, confirmPasswordField;
    private Label feedbackLabel;
    private BitmapFont font;
    private String currentUsername;
    private Imagen fondo;
    private SpriteBatch batch;
    private TopBar topBar;

    public ChangePasswordScreen(String username) {
        this.currentUsername = username;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = UIStyle.getSkin();

        fondo = new Imagen(Resources.STRONGBOX);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = Render.batch;

        topBar = new TopBar(currentUsername, stage, UIStyle.getFont());

        newPasswordField = new TextField("", UIStyle.getTextFieldStyle());
        newPasswordField.setMessageText("Nueva Contraseña");
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');

        confirmPasswordField = new TextField("", UIStyle.getTextFieldStyle());
        confirmPasswordField.setMessageText("Confirmar Contraseña");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');

        feedbackLabel = new Label("", UIStyle.getLabelStyle());
        feedbackLabel.setWrap(true);

        TextButton saveButton = new TextButton("Cambiar Contraseña", UIStyle.getButtonStyle());
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String newPassword = newPasswordField.getText().trim();
                String confirmPassword = confirmPasswordField.getText().trim();

                String validationMessage = InputValidator.validatePassword(newPassword);
                if (validationMessage != null) {
                    feedbackLabel.setText(validationMessage);
                    feedbackLabel.setColor(Color.RED);
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    feedbackLabel.setText("Las contraseñas no coinciden.");
                    feedbackLabel.setColor(Color.RED);
                    return;
                }

                if (UserManager.updatePassword(currentUsername, newPassword)) {
                    feedbackLabel.setText("Contraseña actualizada con éxito.");
                    feedbackLabel.setColor(Color.GREEN);
                } else {
                    feedbackLabel.setText("Error al cambiar la contraseña.");
                    feedbackLabel.setColor(Color.RED);
                }
            }
        });

        TextButton backButton = new TextButton("Volver", UIStyle.getButtonStyle());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Authorities authority = UserManager.getAuthority(currentUsername);
                if (authority == Authorities.ADMIN) {
                    Render.app.setScreen(new AdminPerfilScreen(currentUsername));
                } else if (authority == Authorities.PLAYER) {
                    Render.app.setScreen(new EditProfileScreen(currentUsername));
                }
            }
        });

        Table container = new Table();
        container.setFillParent(true); // Hace que se adapte a cualquier tamaño de pantalla
        container.center(); // Centra el contenido dentro de la tabla principal

        Pixmap pixmap = new Pixmap(600, 400, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.5f, 0.0f, 0.5f, 0.5f)); // morado translúcido
        pixmap.fill();
        TextureRegionDrawable background = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        Table fixedBox = new Table();
        fixedBox.setBackground(background);
        fixedBox.setSize(600, 400);

        Table form = new Table();
        form.center();
        form.add(new Label("Cambiar Contraseña", UIStyle.getLabelStyle())).colspan(2).padBottom(30);
        form.row();
        form.add(newPasswordField).width(400).padBottom(15);
        form.row();
        form.add(confirmPasswordField).width(400).padBottom(15);
        form.row();
        form.add(saveButton).width(300).padTop(15);
        form.row();
        form.add(backButton).width(300).padTop(15);
        form.row();
        form.add(feedbackLabel).width(350).padTop(8);

        fixedBox.add(form).center();
        container.add(fixedBox).width(600).height(400).center();
        stage.addActor(container);



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
        topBar.updateSize();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (font != null) font.dispose();
    }
}
