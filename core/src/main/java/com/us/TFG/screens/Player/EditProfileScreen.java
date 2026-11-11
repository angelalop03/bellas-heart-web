package com.us.TFG.screens.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.UserManager;
import com.us.TFG.utils.*;
import com.us.TFG.utils.validators.InputValidator;

public class EditProfileScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private Imagen fondo;
    private TopBar topBar;
    private TextField usernameField, firstnameField, lastnameField, emailField;
    private Label feedbackLabel;
    private SpriteBatch batch;
    private String currentUsername;
    private Image profileImage;
    private String currentProfileImage;
    private final String[] profileImages = {"default_image.png", "Perfil_2.png", "Perfil_3.jpg", "Perfil_4.jpg"};
    private int currentImageIndex = 0;
    private ImageButton backArrowButton;


    public EditProfileScreen(String username) {
        this.currentUsername = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.WARDROBE);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = UIStyle.getSkin();


        topBar = new TopBar(currentUsername, stage, UIStyle.getFont());

        usernameField = new TextField("", UIStyle.getTextFieldStyle());
        firstnameField = new TextField("", UIStyle.getTextFieldStyle());
        lastnameField = new TextField("", UIStyle.getTextFieldStyle());
        emailField = new TextField("", UIStyle.getTextFieldStyle());

        feedbackLabel = new Label("", UIStyle.getLabelStyle());
        feedbackLabel.setWrap(true);

        // Crear la flecha de retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
         backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100); // Tamaño de la flecha
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150); // Posición en la esquina superior izquierda
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new PlayerPerfilScreen(currentUsername)); // Volver a ForumScreen
            }
        });

        profileImage = new Image();
        Button changeImageButton = new TextButton("Cambiar Foto", UIStyle.getButtonStyle());
        changeImageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changeProfileImage();
            }
        });

        UserManager.loadUserProfile(currentUsername, usernameField, firstnameField, lastnameField, emailField, currentProfileImage, profileImage);

        TextButton saveButton = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updateProfile();
            }
        });

        TextButton passwordButton = new TextButton("Cambiar Contraseña", UIStyle.getButtonStyle());
        passwordButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ChangePasswordScreen(currentUsername));
            }
        });


        Table table = new Table();
        table.setFillParent(true);
        table.row();
        table.row();
        table.add(profileImage).size(100, 100).padTop(50).padBottom(10);
        table.row();
        table.add(changeImageButton).width(200).padBottom(2);
        table.row();
        table.add(usernameField).width(400).padBottom(2);
        table.row();
        table.add(firstnameField).width(400).padBottom(2);
        table.row();
        table.add(lastnameField).width(400).padBottom(2);
        table.row();
        table.add(emailField).width(400).padBottom(2);
        table.row();
        table.add(passwordButton).width(300).padTop(2);
        table.row();
        table.add(saveButton).width(300).padTop(2);
        table.row();
        table.add(feedbackLabel).width(350).padTop(2);

        stage.addActor(table);
        stage.addActor(backArrowButton);

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
        if (backArrowButton != null) {
            backArrowButton.setPosition(10, height - backArrowButton.getHeight() - 50);
        }
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

    private void changeProfileImage() {
        currentImageIndex = (currentImageIndex + 1) % profileImages.length;
        currentProfileImage = profileImages[currentImageIndex];
        profileImage.setDrawable(new TextureRegionDrawable(new Texture(Gdx.files.internal("photos/players/" + currentProfileImage))));
    }

    private void updateProfile() {
        String newUsername = usernameField.getText().trim();
        String firstname = firstnameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String email = emailField.getText().trim();

        String validationMessage = InputValidator.validateInputs(newUsername, firstname, lastname, email);
        if (validationMessage != null) {
            UIStyle.showError(feedbackLabel,validationMessage);
            return;
        }

        if (UserManager.updateProfile(currentUsername, newUsername, firstname, lastname, email, currentProfileImage)) {
            feedbackLabel.setText("Perfil actualizado con éxito.");
            feedbackLabel.setColor(Color.GREEN);
        } else if (UserManager.usernameExists(newUsername)) {
            UIStyle.showError(feedbackLabel,"Nombre de usuario no disponible");
        } else {
            UIStyle.showError(feedbackLabel,"Error al actualizar el perfil");
        }
    }



}
