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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.utils.*;

public class CreditsScreen implements Screen {
    private Stage stage;
    private BitmapFont font;
    private SpriteBatch batch;
    private Imagen fondo;
    private String username;

    public CreditsScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDON);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        font = UIStyle.getFont();

        // === Contenedor principal ===
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // === Recuadro grisáceo translúcido ===
        Table boxTable = new Table();
        boxTable.pad(40);
        boxTable.setBackground(new TextureRegionDrawable(UIStyle.createColoredTexture(1, 1, new Color(0.5f, 0.0f, 0.5f, 0.6f)))); // gris translúcido

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        String texto = "Aplicación desarrollada por:\n" +
            " Ángela López Oliva\n\n"+
            "Trabajo de Fin de Grado - Universidad de Sevilla\n\n" +
            "Grado en Ingeniería Informática - Ingeniería del Software\n\n" +
            "Tutora: Maria Del Carmen Romero Ternero\n" +
            "Co-Tutora: Rosa Maria Casado Mejia\n" +
            "© 2025 Todos los derechos reservados.";

        Label creditos = new Label(texto, style);
        creditos.setWrap(true);
        creditos.setAlignment(Align.center);
        boxTable.add(creditos).width(1000);

        mainTable.add(boxTable);
        stage.addActor(mainTable);

        // === Flecha de retroceso ===
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        ImageButton backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 120);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new PlayerMenuScreen(username)); // Pasa el username si es necesario
            }
        });
        stage.addActor(backArrowButton);
    }



    @Override
    public void render(float delta) {
        batch.begin();
        fondo.draw();
        batch.end();
        stage.act();
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
