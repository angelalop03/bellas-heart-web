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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.screens.Admin.Game.AdminGameScreen;
import com.us.TFG.utils.*;

public class AdminMenuScreen implements Screen {

    private Stage stage;
    private SpriteBatch b;
    private Skin skin;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;

    public AdminMenuScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.MENU);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        b = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        font = UIStyle.getFont();
        topBar = new TopBar(username, stage, font);
        skin = new Skin();
        skin.add("Caveat", font);

        TextButton playerButton = new TextButton("Players", UIStyle.getButtonStyle());
        TextButton foroButton = new TextButton("Foro", UIStyle.getButtonStyle());
        TextButton historyButton = new TextButton("Historias", UIStyle.getButtonStyle());

        // Crear tabla contenedora para centrar botones
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().center().padTop(150);

        // Añadir botones con padding
        table.add(playerButton).width(300).height(75).pad(10).row();
        table.add(foroButton).width(300).height(75).pad(10).row();
        table.add(historyButton).width(300).height(75).pad(10).row();

        // Listeners
        playerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminViewPlayersScreen(username));
            }
        });

        foroButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminForumScreen(username));
            }
        });

        historyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminGameScreen(username));
            }
        });

        // Agregar tabla al stage
        stage.addActor(table);
    }

    @Override
    public void render(float v) {
        b.begin();
        fondo.draw();
        b.end();

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
    @Override public void dispose() {
        stage.dispose();
        font.dispose();
    }


}
