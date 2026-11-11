package com.us.TFG.screens.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.PlayerManager;
import com.us.TFG.manager.UserManager;
import com.us.TFG.screens.General.OpenScreen;
import com.us.TFG.screens.Player.Game.CreateGameScreen;
import com.us.TFG.utils.*;

public class PlayerPerfilScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private Skin skin;
    private TopBar topBar;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String image;

    public PlayerPerfilScreen(String username) {
        this.username = username;
        PlayerManager.PlayerData playerData = PlayerManager.getPlayerData(username);
        if (playerData != null) {
            this.firstname = playerData.getFirstname();
            this.lastname = playerData.getLastname();
            this.email = playerData.getEmail();
            this.image = playerData.getImage();
        }
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.WARDROBE);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = UIStyle.getSkin();

        topBar = new TopBar(username, stage, UIStyle.getFont());

        Table table = new Table();
        table.setFillParent(true);

        Image playerImage = new Image(new Texture(Gdx.files.internal("photos/players/" + image)));
        table.add(playerImage).size(200, 200).padBottom(20);
        table.row();

        table.add(new Label("Nombre: " + firstname + " " + lastname, UIStyle.getLabelStyle())).padBottom(10);
        table.row();

        table.add(new Label("Email: " + email, UIStyle.getLabelStyle())).padBottom(10);
        table.row();



        TextButton editProfileButton = new TextButton("Editar Perfil", UIStyle.getButtonStyle());
        editProfileButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new EditProfileScreen(username));
            }
        });
        table.add(editProfileButton).size(200, 50).padBottom(10);
        table.row();

        TextButton logOutButton = new TextButton("Cerrar Sesión", UIStyle.getButtonStyle());
        logOutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showLogoutDialog();
            }
        });
        table.add(logOutButton).size(200, 50).padBottom(10);
        table.row();


        TextButton deleteProfileButton = new TextButton("Eliminar Perfil", UIStyle.getButtonStyle());
        deleteProfileButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                showDeleteProfileDialog();
            }
        });
        table.add(deleteProfileButton).size(200, 50).padBottom(10);
        table.row();

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
        topBar.updateSize();
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
    }

    private void showDeleteProfileDialog() {
        // Crear estilo del diálogo
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = UIStyle.getFont();
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(500, 300, Color.RED, 1f)));

        Dialog deleteDialog = new Dialog("", dialogStyle);

        // Crear campo para ingresar contraseña
        TextField passwordField = new TextField("", UIStyle.getTextFieldStyle());
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        // Etiqueta para mostrar mensajes de feedback
        Label feedbackLabel = new Label("", UIStyle.getLabelStyle());
        feedbackLabel.setWrap(false);
        feedbackLabel.setAlignment(Align.center);

        // Botón de confirmar
        TextButton confirmButton = new TextButton("Confirmar", UIStyle.getButtonStyle());
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String password = passwordField.getText().trim();

                if (UserManager.verifyLogin(username, password)) {
                    if (UserManager.deleteUser(username)) {
                        feedbackLabel.setText("Perfil eliminado con éxito.");
                        feedbackLabel.setColor(Color.GREEN);

                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                Render.app.setScreen(new OpenScreen()); // Redirigir a la pantalla OpenScreen
                            }
                        }, 3);                    }
                    else {
                        feedbackLabel.setText("Error al eliminar el perfil.");
                        feedbackLabel.setColor(Color.RED);
                    }
                } else {
                    feedbackLabel.setText("Contraseña incorrecta.");
                    feedbackLabel.setColor(Color.RED);
                }

            }
        });
        // Botón de cancelar
        TextButton cancelButton = new TextButton("Cerrar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deleteDialog.hide();
            }
        });
        deleteDialog.getContentTable().add(new Label("¿Estás seguro de borrar tu perfil?", UIStyle.getLabelStyle())).padBottom(5).padTop(25).row();
        deleteDialog.getContentTable().add(new Label("Introduce tu contraseña:", UIStyle.getLabelStyle())).padBottom(5).row();
        deleteDialog.getContentTable().add(passwordField).width(300).pad(5).padBottom(5).row();
        deleteDialog.getContentTable().add(feedbackLabel).colspan(2).center().pad(10).row();
        deleteDialog.getButtonTable().add(confirmButton).padBottom(10);
        deleteDialog.getButtonTable().add(cancelButton).padBottom(10);

        deleteDialog.show(stage);
    }


    private void showLogoutDialog() {
        // Crear estilo del diálogo
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = UIStyle.getFont();
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(500, 300, Color.PINK, 1f)));

        Dialog logoutDialog = new Dialog("", dialogStyle);

        // Etiqueta de confirmación
        Label confirmLabel = new Label("¿Estás seguro de que deseas cerrar sesión?", UIStyle.getLabelStyle());

        // Botón de confirmar
        TextButton confirmButton = new TextButton("Confirmar", UIStyle.getButtonStyle());
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new OpenScreen());
            }
        });

        // Botón de cancelar
        TextButton cancelButton = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logoutDialog.hide();
            }
        });

        logoutDialog.getContentTable().add(confirmLabel).padBottom(10).row();
        logoutDialog.getButtonTable().add(confirmButton).pad(10);
        logoutDialog.getButtonTable().add(cancelButton).pad(10);

        logoutDialog.show(stage);
    }

}
