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

import java.util.List;

public class ForumScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Skin skin;
    private Imagen fondo;
    private TopBar topBar;
    private String username;
    private ImageButton backArrowButton;

    public ForumScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.NEWSPAPER);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = UIStyle.getSkin();

        topBar = new TopBar(username, stage, UIStyle.getFont());

        Table mainTable = new Table();
        mainTable.padTop(60).setFillParent(true);

        Table newsTable = new Table();
        newsTable.top().padTop(50);

        List<NewsManager.NewsData> newsList = NewsManager.getAllNews();
        java.util.Collections.reverse(newsList);

        for (NewsManager.NewsData news : newsList) {
            Table newsContainer = new Table();
            newsContainer.setBackground(UIStyle.getBackgroundDrawable());

            Label newsTitleLabel = new Label(news.getName(), UIStyle.getTitleLabelStyle());
            newsTitleLabel.setWrap(true);
            newsTitleLabel.setAlignment(1);
            newsContainer.add(newsTitleLabel).width(1000).center().pad(10).row();

            newsContainer.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Render.app.setScreen(new NewsDetailScreen(username, news));
                }
            });

            newsTable.add(newsContainer).padTop(5).center().pad(10).row();
        }

        ScrollPane scrollPane = new ScrollPane(newsTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollbarsVisible(true);
        scrollPane.setScrollBarPositions(false, true);

        mainTable.add(scrollPane).fill().expand();
        stage.addActor(mainTable);

        // Flecha de retroceso (siempre arriba a la izquierda)
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 110);

        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new PlayerMenuScreen(username));
            }
        });

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

        // Reposicionar flecha tras redimensionar
        if (backArrowButton != null) {
            backArrowButton.setPosition(10, height - 150);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
