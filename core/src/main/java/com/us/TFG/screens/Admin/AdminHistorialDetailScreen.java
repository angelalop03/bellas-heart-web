package com.us.TFG.screens.Admin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.us.TFG.manager.*;
import com.us.TFG.model.*;
import com.us.TFG.utils.*;

import java.util.List;

public class AdminHistorialDetailScreen implements Screen {
    private String adminUsername;
    private String playerUsername;
    private int gameId;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private Stage stage;
    private SpriteBatch batch;
    private static final int DECISIONS_PER_PAGE = 1;
    private int currentPage = 0;
    private List<PlayerDecisionState> decisions;
    private ImageButton backArrowButton, flechaIzqButton, flechaDerButton;

    public AdminHistorialDetailScreen(String adminUsername, String playerUsername, int gameId) {
        this.adminUsername = adminUsername;
        this.playerUsername = playerUsername;
        this.gameId = gameId;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDOBLOC);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        font = UIStyle.getFont();
        topBar = new TopBar(adminUsername, stage, font);

        int playerId = PlayerManager.getPlayerData(playerUsername).getId();
        decisions = PlayerDecisionStateManager.getPositivePointDecisions(playerId, gameId);

        renderPage();
    }

    private void renderPage() {
        stage.clear();
        topBar = new TopBar(adminUsername, stage, font);

        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - backArrowButton.getHeight() - 50);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminHistorialScreen(adminUsername, playerUsername));
            }
        });
        stage.addActor(backArrowButton);

        Table decisionTable = new Table();
        decisionTable.setFillParent(true);
        decisionTable.top().padTop(180);

        if (decisions == null || decisions.isEmpty()) {
            Label noDataLabel = new Label("No hay decisiones disponibles", new Label.LabelStyle(font, Color.RED));
            decisionTable.add(noDataLabel).pad(10).row();
        } else {
            int index = currentPage;
            if (index < decisions.size()) {
                PlayerDecisionState decisionState = decisions.get(index);

                try {
                    Option option = OptionManager.getOptionById(decisionState.getOptionId());
                    Decision decision = DecisionManager.getDecisionById(option.getDecisionId());
                    Scene scene = SceneManager.getSceneById(decision.getSceneId());
                    Chapter chapter = ChapterManager.getChapterById(scene.getChapterId());

                    String chapterTitle = chapter.getName();
                    String optionText = option.getText();

                    String info = "Capítulo: " + chapterTitle + "\n" +
                        "Opción elegida: " + optionText;

                    Table decisionBox = new Table();
                    decisionBox.pad(30);
                    decisionBox.setBackground(new TextureRegionDrawable(UIStyle.createColoredTexture(1, 1, new Color(1f, 0.7f, 0.8f, 0.5f))));

                    String chapterImgPath = chapter.getImagePath();
                    Texture chapterTexture = new Texture(Gdx.files.internal("photos/Chapters/" + chapterImgPath));
                    Image chapterImage = new Image(chapterTexture);
                    chapterImage.setSize(180, 180);

                    Label label = new Label(info, new Label.LabelStyle(font, Color.WHITE));
                    label.setWrap(true);
                    label.setWidth(600);
                    label.setAlignment(Align.left);

                    Table contentTable = new Table();
                    contentTable.add(chapterImage).padRight(25).width(180).height(180);
                    contentTable.add(label).width(600).pad(10);

                    decisionBox.add(contentTable).center();
                    decisionBox.row();

                    decisionTable.add(decisionBox).pad(10).width(900).padTop(50).center().row();
                } catch (Exception e) {
                    Label errorLabel = new Label("Error al obtener información de la decisión.", new Label.LabelStyle(font, Color.RED));
                    decisionTable.add(errorLabel).pad(10).row();
                    e.printStackTrace();
                }
            }

            int totalPages = (int) Math.ceil((float) decisions.size() / DECISIONS_PER_PAGE);
            String pageText = (currentPage + 1) + " / " + totalPages;
            Label pageLabel = new Label(pageText, new Label.LabelStyle(font, Color.WHITE));
            pageLabel.setAlignment(Align.center);
            decisionTable.add(pageLabel).padBottom(20).center().row();

            if (flechaIzqButton != null) flechaIzqButton.remove();
            if (flechaDerButton != null) flechaDerButton.remove();

            Texture flechaIzqTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
            flechaIzqButton = new ImageButton(new TextureRegionDrawable(flechaIzqTexture));
            flechaIzqButton.setSize(100, 100);
            flechaIzqButton.setPosition(20, 20);
            flechaIzqButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentPage > 0) {
                        currentPage--;
                        renderPage();
                    }
                }
            });

            Texture flechaDerTexture = new Texture(Gdx.files.internal("photos/flechaD.png"));
            flechaDerButton = new ImageButton(new TextureRegionDrawable(flechaDerTexture));
            flechaDerButton.setSize(100, 100);
            flechaDerButton.setPosition(Gdx.graphics.getWidth() - 120, 20);
            flechaDerButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentPage + 1 < decisions.size()) {
                        currentPage++;
                        renderPage();
                    }
                }
            });

            stage.addActor(flechaIzqButton);
            stage.addActor(flechaDerButton);
        }

        stage.addActor(decisionTable);
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
        if (flechaIzqButton != null) {
            flechaIzqButton.setPosition(20, 20);
        }
        if (flechaDerButton != null) {
            flechaDerButton.setPosition(width - 120, 20);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
