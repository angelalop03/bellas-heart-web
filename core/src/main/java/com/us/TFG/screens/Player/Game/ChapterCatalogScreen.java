package com.us.TFG.screens.Player.Game;

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
import com.us.TFG.manager.ChapterManager;
import com.us.TFG.manager.PlayerManager;
import com.us.TFG.model.Chapter;
import com.us.TFG.utils.*;

import java.util.List;

public class ChapterCatalogScreen implements Screen {

    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private int gameId;
    private int currentPage = 0;
    private static final int CHAPTERS_PER_PAGE = 5;
    private static final int MAX_UNLOCKED_CHAPTERS = 9;
    private List<Chapter> chapters;

    private ImageButton flechaDButton;
    private ImageButton flechaButton;
    private ImageButton backArrowButton;
    private Table chapterTable;
    private Label pageLabel;

    public ChapterCatalogScreen(String username, int gameId) {
        this.username = username;
        this.gameId = gameId;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDOBOOK3);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        font = UIStyle.getFont();
        chapters = ChapterManager.getChaptersForPlayerandGame(PlayerManager.getPlayerData(username).getId(), gameId);
        chapters.removeIf(chapter -> "0".equals(chapter.getChapterNumber()));

        renderPage();
    }

    private void renderPage() {
        stage.clear();
        topBar = new TopBar(username, stage, font);

        // Flecha retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ChargeGameScreen(username));
            }
        });

        // Flechas navegación
        Texture flechaDTexture = new Texture(Gdx.files.internal("photos/flechaD.png"));
        flechaDButton = new ImageButton(new TextureRegionDrawable(flechaDTexture));
        flechaDButton.setSize(100, 100);
        flechaDButton.setPosition(Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 750);

        Texture flechaTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        flechaButton = new ImageButton(new TextureRegionDrawable(flechaTexture));
        flechaButton.setSize(100, 100);
        flechaButton.setPosition(20, Gdx.graphics.getHeight() - 750);

        // Tabla de capítulos
        chapterTable = new Table();
        chapterTable.setFillParent(false);
        chapterTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        chapterTable.setPosition(0, 30);
        chapterTable.padLeft(150).padRight(150);

        TextButton.TextButtonStyle unlockedStyle = UIStyle.getCustomButtonStyle(300, 150, new Color(0.5f, 0.0f, 0.5f, 1f));
        TextButton.TextButtonStyle lockedStyle = UIStyle.getCustomButtonStyle(300, 150, new Color(0.5f, 0.5f, 0.5f, 0.5f));
        lockedStyle.fontColor = Color.DARK_GRAY;

        int start = currentPage * CHAPTERS_PER_PAGE;
        int end = Math.min(start + CHAPTERS_PER_PAGE, MAX_UNLOCKED_CHAPTERS);

        for (int i = start; i < end; i++) {
            Table row = new Table();

            if (i < chapters.size()) {
                Chapter chapter = chapters.get(i);
                Texture imgTex = new Texture(Gdx.files.internal("photos/Chapters/" + chapter.getImagePath()));
                Image img = new Image(new TextureRegionDrawable(imgTex));
                img.setSize(100, 100);
                row.add(img).size(100, 100).padRight(20).padLeft(40);

                TextButton button = new TextButton(chapter.getChapterNumber() + "\n" + chapter.getName(), unlockedStyle);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Render.app.setScreen(new SceneGameScreen(gameId, chapter.getId(), username));
                    }
                });
                row.add(button).width(290).height(100).padRight(40);
            } else {
                Texture defaultImgTex = new Texture(Gdx.files.internal("photos/Chapters/Default_Chapter.png"));
                Image img = new Image(new TextureRegionDrawable(defaultImgTex));
                img.setSize(100, 100);
                row.add(img).size(100, 100).padRight(20).padLeft(40);

                TextButton button = new TextButton("No desbloqueado", lockedStyle);
                row.add(button).width(290).height(100).padRight(40);
            }

            chapterTable.add(row).padBottom(20);
            if ((i - start + 1) % 2 == 0) chapterTable.row();
        }

        // Click pantalla para cambiar de página
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if ((currentPage + 1) * CHAPTERS_PER_PAGE < MAX_UNLOCKED_CHAPTERS) {
                    currentPage++;
                } else {
                    currentPage = 0;
                }
                renderPage();
            }
        });

        // Paginación
        int totalPages = (int) Math.ceil((float) MAX_UNLOCKED_CHAPTERS / CHAPTERS_PER_PAGE);
        String pageText = (currentPage + 1) + " / " + totalPages;
        pageLabel = new Label(pageText, new Label.LabelStyle(font, Color.WHITE));
        pageLabel.setAlignment(Align.center);
        pageLabel.setFontScale(1.2f);
        pageLabel.setPosition((Gdx.graphics.getWidth() - pageLabel.getPrefWidth()) / 2f, 10);

        // Añadir a stage
        stage.addActor(chapterTable);
        stage.addActor(backArrowButton);
        stage.addActor(flechaDButton);
        stage.addActor(flechaButton);
        stage.addActor(pageLabel);
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

        renderPage();  // Redibujar todos los elementos adaptados
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
}
