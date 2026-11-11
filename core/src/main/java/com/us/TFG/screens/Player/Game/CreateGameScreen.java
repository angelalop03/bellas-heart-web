package com.us.TFG.screens.Player.Game;

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
import com.us.TFG.manager.HistoryManager;
import com.us.TFG.manager.PlayerManager;
import com.us.TFG.model.History;
import com.us.TFG.screens.Player.PlayerMenuScreen;
import com.us.TFG.utils.*;

import java.util.List;

public class CreateGameScreen implements Screen {

    private Stage stage;
    private SpriteBatch batch;
    private Skin skin;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private List<History> histories;
    private int currentIndex;
    private ScrollPane scrollPane;
    private ImageButton backArrowButton;


    public CreateGameScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDODOORS);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        font = UIStyle.getFont();
        topBar = new TopBar(username, stage, font);

        // Cargar las partidas del jugador
        histories = HistoryManager.getHistoriesPublished();
        currentIndex = 0;

        // Crear la flecha de retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
         backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new PlayerMenuScreen(username));
            }
        });

        // Crear el contenedor principal
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Pixmap pixmap = new Pixmap(1000, 400, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1.0f, 0.41f, 0.71f, 0.6f));
        pixmap.fill();
        TextureRegionDrawable background = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        Table contentTable = new Table();
        contentTable.setBackground(background);

        // Título
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        Label titleLabel = new Label("Historias", labelStyle);
        contentTable.add(titleLabel).colspan(3).padBottom(20);
        contentTable.row();

        if (histories.isEmpty()) {
            Label noGamesLabel = new Label("No hay historias disponibles", labelStyle);
            contentTable.add(noGamesLabel).colspan(3).padTop(50);
        } else if (histories.size() == 1) {
            History history = histories.get(0);

            Table cardTable = new Table(skin);
            cardTable.setBackground(new TextureRegionDrawable(new Texture("photos/Histories/" + history.getImagePath())));
            Label gameNameLabel = new Label(history.getTitle(), labelStyle);
            cardTable.add(gameNameLabel).center().pad(10);

            cardTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Render.app.setScreen(new HistoryDetailScreen(username, history));
                }
            });
            contentTable.add(cardTable).width(300).height(200).padTop(50).colspan(3);
        } else {
            // Flecha izquierda
            Texture leftArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
            ImageButton leftArrowButton = new ImageButton(new TextureRegionDrawable(leftArrowTexture));
            leftArrowButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentIndex > 0) {
                        currentIndex--;
                        scrollPane.setScrollX(scrollPane.getScrollX() - 320);
                    }
                }
            });

            // Flecha derecha
            Texture rightArrowTexture = new Texture(Gdx.files.internal("photos/flechaD.png"));
            ImageButton rightArrowButton = new ImageButton(new TextureRegionDrawable(rightArrowTexture));
            rightArrowButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentIndex < histories.size() - 1) {
                        currentIndex++;
                        scrollPane.setScrollX(scrollPane.getScrollX() + 320);
                    }
                }
            });

            // Crear carrusel de tarjetas
            ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
            Table carouselTable = new Table();

            for (History history : histories) {
                Table cardTable = new Table(skin);
                cardTable.setBackground(new TextureRegionDrawable(new Texture("photos/Histories/" + history.getImagePath())));
                Label gameNameLabel = new Label(history.getTitle(), labelStyle);
                cardTable.add(gameNameLabel).center().pad(10);

                cardTable.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Render.app.setScreen(new HistoryDetailScreen(username, history));
                    }
                });

                carouselTable.add(cardTable).width(300).height(200).pad(20);
            }

            scrollPane = new ScrollPane(carouselTable, scrollStyle);
            scrollPane.setScrollingDisabled(false, true);

            contentTable.add(leftArrowButton).width(100).height(100);
            contentTable.add(scrollPane).expandX().fillX().pad(20);
            contentTable.add(rightArrowButton).width(100).height(100);
        }

        // Añadir los elementos a la escena
        mainTable.add(contentTable).center();
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
        font.dispose();
    }
}
