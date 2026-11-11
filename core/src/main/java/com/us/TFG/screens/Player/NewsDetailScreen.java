package com.us.TFG.screens.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.NewsManager;
import com.us.TFG.utils.*;

public class NewsDetailScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private NewsManager.NewsData news;
    private Skin skin;
    private TopBar topBar;
    private String username;
    private ImageButton backArrowButton;
    private Table wrapper;

    public NewsDetailScreen(String username, NewsManager.NewsData news) {
        this.username = username;
        this.news = news;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.NEWS);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = UIStyle.getSkin();

        topBar = new TopBar(username, stage, UIStyle.getFont());

        // Crear la flecha de retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - backArrowButton.getHeight() - 10);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ForumScreen(username));
            }
        });

        // Contenedor con fondo para la noticia
        Table contentTable = new Table();
        contentTable.top().pad(30).padTop(50);
        contentTable.setBackground(UIStyle.getBackgroundDrawable());

        // Título de la noticia
        Label newsTitleLabel = new Label(news.getName(), UIStyle.getTitleLabelStyle());
        newsTitleLabel.setWrap(true);
        newsTitleLabel.setAlignment(1);
        contentTable.add(newsTitleLabel).width(Value.percentWidth(0.9f, contentTable)).center().pad(10).row();

        // Contenido
        Label contentLabel = new Label(news.getText(), UIStyle.getLabelStyle());
        contentLabel.setWrap(true);
        contentTable.add(contentLabel).width(Value.percentWidth(0.9f, contentTable)).center().pad(10).row();

        // ScrollPane (por si es largo)
        ScrollPane scrollPane = new ScrollPane(contentTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // Envolver scrollPane en una tabla centrada
        wrapper = new Table();
        wrapper.setFillParent(true);
        wrapper.add(scrollPane)
            .width(Value.percentWidth(0.95f, wrapper))
            .height(Value.percentHeight(0.85f, wrapper))
            .center()
            .padTop(70);

        // Añadir al stage
        stage.addActor(wrapper);
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
