package com.us.TFG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.us.TFG.manager.UserManager;
import com.us.TFG.model.Authorities;
import com.us.TFG.screens.Admin.AdminMenuScreen;
import com.us.TFG.screens.Admin.AdminPerfilScreen;
import com.us.TFG.screens.Player.PlayerMenuScreen;
import com.us.TFG.screens.Player.PlayerPerfilScreen;

public class TopBar {

    private Table topBar;

    public TopBar(String username, Stage stage, BitmapFont font) {
        // Crear el fondo morado de la barra con altura fija
        int barHeight = 60;
        Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), barHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.5f, 0.0f, 0.5f, 1.0f));  // Color morado
        pixmap.fill();
        TextureRegionDrawable topBarBackground = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        // Crear la tabla para la barra superior
        topBar = new Table();
        topBar.setBackground(topBarBackground);
        topBar.setSize(Gdx.graphics.getWidth(), barHeight); // Ancho completo y altura fija
        topBar.setPosition(0, Gdx.graphics.getHeight() - barHeight); // Colocar en la parte superior de la pantalla

        // Botón del logo de la app
        Texture logoTexture = new Texture(Gdx.files.internal("photos/logo.png"));
        ImageButton logoButton = new ImageButton(new TextureRegionDrawable(logoTexture));
        logoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Authorities authority = UserManager.getAuthority(username);
                if (authority == Authorities.ADMIN) {
                    Render.app.setScreen(new AdminMenuScreen(username));
                } else if (authority == Authorities.PLAYER) {
                    Render.app.setScreen(new PlayerMenuScreen(
                        username
                    ));                }
                // Redirigir al menú principal

            }
        });

        // Botón con el nombre de usuario
        TextButton.TextButtonStyle usernameStyle = new TextButton.TextButtonStyle();
        usernameStyle.font = font;
        usernameStyle.fontColor = Color.WHITE;
        TextButton usernameButton = new TextButton(username, usernameStyle);
        usernameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Authorities authority = UserManager.getAuthority(username);
                if (authority == Authorities.ADMIN) {
                    Render.app.setScreen(new AdminPerfilScreen(username));

                } else if (authority == Authorities.PLAYER) {
                    Render.app.setScreen(new PlayerPerfilScreen(username));
                }
            }
        });

        // Agregar elementos a la barra superior
        topBar.add(logoButton).size(120, 120).pad(10);
        topBar.add().expandX();  // Espacio para centrar
        Authorities authority = UserManager.getAuthority(username);

            topBar.add(usernameButton).padRight(20);


        // Añadir la barra al stage
        stage.addActor(topBar);
    }

    public Table getTopBar() {
        return topBar;
    }

    public void updateSize() {
        int barHeight = 60;
        topBar.setSize(Gdx.graphics.getWidth(), barHeight);
        topBar.setPosition(0, Gdx.graphics.getHeight() - barHeight);
    }

}
