package com.us.TFG.screens.Admin;

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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.PlayerManager;
import com.us.TFG.manager.UserManager;
import com.us.TFG.screens.Player.PlayerHistorialScreen;
import com.us.TFG.utils.*;
import com.us.TFG.utils.validators.InputValidator;

import java.util.List;

public class AdminViewPlayersScreen implements Screen {

    private String adminUsername;
    private Imagen fondo;
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private BitmapFont font;
    private TopBar topBar;

    private ScrollPane scrollPane;
    private Image background;
    private ImageButton backArrowButton;


    public AdminViewPlayersScreen(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDON);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;
        font = UIStyle.getFont();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin();
        skin.add("Caveat", font);

        topBar = new TopBar(adminUsername, stage, font);

        Pixmap pixmap = new Pixmap(1000, 500, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.8f, 0.8f, 0.8f, 0.6f));
        pixmap.fill();
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        background = new Image(backgroundDrawable);
        stage.addActor(background);

        Table playersTable = createPlayersTable(UIStyle.getLabelStyle());

        scrollPane = new ScrollPane(playersTable);
        scrollPane.setSize(1000, 500);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(true);
        stage.addActor(scrollPane);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150); // Posicionado arriba a la izquierda
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminMenuScreen(adminUsername)); // Ajusta al destino correcto
            }
        });
        stage.addActor(backArrowButton);

    }

    private Table createPlayersTable(Label.LabelStyle labelStyle) {
        Table table = new Table();
        table.top();

        List<PlayerManager.PlayerData> players = PlayerManager.getAllPlayers();
        for (PlayerManager.PlayerData player : players) {
            table.add(new Label(player.getFirstname() + " " + player.getLastname(), labelStyle)).width(300).pad(10).center();
            table.add(new Label(player.getEmail(), labelStyle)).width(300).pad(10).center();

            TextButton editButton = new TextButton("Editar", UIStyle.getButtonStyle());
            editButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showEditDialog(player);
                }
            });
            table.add(editButton).width(100).pad(10).center();

            Table actionTable = new Table();

            TextButton historyButton = new TextButton("Historial", UIStyle.getButtonStyle());
            historyButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Render.app.setScreen(new AdminHistorialScreen(adminUsername, UserManager.findUserByPlayerId(player.getId())));
                }
            });
            actionTable.add(historyButton).width(110).padRight(10);

            TextButton deleteButton = new TextButton("Eliminar", UIStyle.getButtonStyle());
            deleteButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showDeleteDialog(player);
                }
            });
            actionTable.add(deleteButton).width(100);

            table.add(actionTable).pad(10).center();
            table.row();
        }

        return table;
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
        float centeredX = (width - 1000) / 2f;
        float yPosition = 100;

        if (scrollPane != null) {
            scrollPane.setPosition(centeredX, yPosition);
        }
        if (background != null) {
            background.setPosition(centeredX, yPosition);
        }
        if (backArrowButton != null) {
            backArrowButton.setPosition(10, height - 150); // Reposiciona arriba izq
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
        font.dispose();
    }

    private void showEditDialog(PlayerManager.PlayerData player) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(400, 300, Color.VIOLET, 1.0f)));

        Dialog editDialog = new Dialog("", dialogStyle);

        TextField usernameField = new TextField(PlayerManager.getUsername(player.getId()), UIStyle.getTextFieldStyle());
        TextField firstnameField = new TextField(player.getFirstname(), UIStyle.getTextFieldStyle());
        TextField lastnameField = new TextField(player.getLastname(), UIStyle.getTextFieldStyle());
        TextField emailField = new TextField(player.getEmail(), UIStyle.getTextFieldStyle());

        editDialog.getContentTable().add(new Label("Usuario:", new Label.LabelStyle(font, Color.WHITE))).pad(5);
        editDialog.getContentTable().add(usernameField).width(300).pad(5).row();
        editDialog.getContentTable().add(new Label("Nombre:", new Label.LabelStyle(font, Color.WHITE))).pad(5);
        editDialog.getContentTable().add(firstnameField).width(300).pad(5).row();
        editDialog.getContentTable().add(new Label("Apellido:", new Label.LabelStyle(font, Color.WHITE))).pad(5);
        editDialog.getContentTable().add(lastnameField).width(300).pad(5).row();
        editDialog.getContentTable().add(new Label("Correo:", new Label.LabelStyle(font, Color.WHITE))).pad(5);
        editDialog.getContentTable().add(emailField).width(300).pad(5).row();

        Label feedbackLabel = new Label("", UIStyle.getLabelStyle());
        feedbackLabel.setWrap(false);
        feedbackLabel.setAlignment(Align.center);

        TextButton changePasswordButton = new TextButton("Cambiar Contraseña", UIStyle.getButtonStyle());
        changePasswordButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showChangePasswordDialog(player);
                editDialog.hide();
                editDialog.remove();
            }
        });
        editDialog.getContentTable().add(changePasswordButton).colspan(2).center().pad(10).row();

        TextButton saveButton = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String usernameError = InputValidator.validateUsername(usernameField.getText());
                String firstnameError = InputValidator.validateFirstname(firstnameField.getText());
                String lastnameError = InputValidator.validateLastname(lastnameField.getText());
                String emailError = InputValidator.validateEmail(emailField.getText());

                if (usernameError != null || firstnameError != null || lastnameError != null || emailError != null) {
                    feedbackLabel.setText(usernameError != null ? usernameError :
                        firstnameError != null ? firstnameError :
                            lastnameError != null ? lastnameError : emailError);
                    feedbackLabel.setColor(Color.RED);
                } else {
                    boolean success = UserManager.updateProfile(
                        PlayerManager.getUsername(player.getId()),
                        usernameField.getText(),
                        firstnameField.getText(),
                        lastnameField.getText(),
                        emailField.getText(),
                        player.getImage()
                    );

                    if (success) {
                        feedbackLabel.setText("Perfil actualizado con éxito.");
                        feedbackLabel.setColor(Color.GREEN);
                    } else {
                        feedbackLabel.setText("Error al actualizar el perfil.");
                        feedbackLabel.setColor(Color.RED);
                    }
                }
            }
        });
        editDialog.getButtonTable().add(saveButton).pad(10);

        TextButton cancelButton = new TextButton("Cerrar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editDialog.hide();
                editDialog.remove();
            }
        });
        editDialog.getButtonTable().add(cancelButton).pad(10);
        editDialog.getContentTable().add(feedbackLabel).colspan(2).center().pad(10).row();

        editDialog.show(stage);
    }

    private void showChangePasswordDialog(PlayerManager.PlayerData player) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(500, 300, Color.PINK, 1.0f)));

        Dialog passwordDialog = new Dialog("", dialogStyle);

        Label feedbackLabel = new Label("", UIStyle.getLabelStyle());
        feedbackLabel.setWrap(false);
        feedbackLabel.setAlignment(Align.center);

        TextField newPasswordField = new TextField("", UIStyle.getTextFieldStyle());
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');

        TextField confirmPasswordField = new TextField("", UIStyle.getTextFieldStyle());
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');

        passwordDialog.getContentTable().add(new Label("Nueva Contraseña:", new Label.LabelStyle(font, Color.WHITE))).pad(5);
        passwordDialog.getContentTable().add(newPasswordField).width(300).pad(5).row();
        passwordDialog.getContentTable().add(new Label("Confirmar Contraseña:", new Label.LabelStyle(font, Color.WHITE))).pad(5);
        passwordDialog.getContentTable().add(confirmPasswordField).width(300).pad(5).row();

        TextButton saveButton = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String passwordError = InputValidator.validatePassword(newPasswordField.getText());
                if (passwordError != null) {
                    feedbackLabel.setText(passwordError);
                    feedbackLabel.setColor(Color.RED);
                } else if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    feedbackLabel.setText("Las contraseñas no coinciden.");
                    feedbackLabel.setColor(Color.RED);
                } else {
                    boolean success = UserManager.updatePassword(
                        PlayerManager.getUsername(player.getId()),
                        newPasswordField.getText()
                    );

                    if (success) {
                        feedbackLabel.setText("Contraseña actualizada con éxito.");
                        feedbackLabel.setColor(Color.GREEN);
                    } else {
                        feedbackLabel.setText("Error al actualizar la contraseña.");
                        feedbackLabel.setColor(Color.RED);
                    }
                }
            }
        });

        TextButton cancelButton = new TextButton("Cerrar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                passwordDialog.hide();
                passwordDialog.remove();
                showEditDialog(player);
            }
        });

        passwordDialog.getButtonTable().add(saveButton).pad(10);
        passwordDialog.getButtonTable().add(cancelButton).pad(10);
        passwordDialog.getContentTable().add(feedbackLabel).colspan(2).center().pad(10).row();

        passwordDialog.show(stage);
    }

    private void showDeleteDialog(PlayerManager.PlayerData player) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(400, 300, Color.RED, 1f)));

        Dialog deleteDialog = new Dialog("", dialogStyle);
        deleteDialog.text(new Label("¿Estás seguro de eliminar este perfil?", UIStyle.getLabelStyle())).pad(10);

        TextButton confirmButton = new TextButton("Confirmar", UIStyle.getButtonStyle());
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (UserManager.deleteUser(PlayerManager.getUsername(player.getId()))) {
                    deleteDialog.hide();
                    deleteDialog.remove();
                    Render.app.setScreen(new AdminViewPlayersScreen(adminUsername));
                } else {
                    deleteDialog.text(new Label("Error al eliminar el perfil.", UIStyle.getLabelStyle())).pad(10);
                }
            }
        });

        TextButton cancelButton = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deleteDialog.hide();
                deleteDialog.remove();
            }
        });

        deleteDialog.getButtonTable().add(confirmButton).pad(10);
        deleteDialog.getButtonTable().add(cancelButton).pad(10);

        deleteDialog.show(stage);
    }
}
