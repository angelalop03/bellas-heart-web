package com.us.TFG.screens.Admin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.NewsManager;
import com.us.TFG.utils.Config;
import com.us.TFG.utils.Render;
import com.us.TFG.utils.Resources;
import com.us.TFG.utils.TopBar;

import java.util.Collections;
import java.util.List;

public class AdminForumScreen implements Screen {

    private Stage stage;
    private SpriteBatch b;
    private Skin skin;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private ImageButton backArrowButton;


    public AdminForumScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        // Cargar el fondo
        fondo = new Imagen(Resources.FONDON);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        b = Render.batch;

        // Crear Stage y Skin
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin();

        // Generar la fuente
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Caveat.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 34;
        font = generator.generateFont(parameter);
        generator.dispose();

        // Añadir la fuente generada al skin
        skin.add("Caveat", font);

        // Crear la barra superior
        topBar = new TopBar(username, stage, font);

        // Crear el contenedor principal
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        // Crear botones de acción
        Table buttonTable = new Table();
        buttonTable.top().pad(10);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = new TextureRegionDrawable(createBackground(300, 60, Color.DARK_GRAY));
        buttonStyle.down = new TextureRegionDrawable(createBackground(300, 60, Color.GRAY));

        TextButton createButton = new TextButton("Crear Noticia", buttonStyle);
        createButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminCreateNewsScreen(username));
            }
        });

        buttonTable.add(createButton).pad(10).padTop(60);

        mainTable.add(buttonTable).expandX().padBottom(20).row();

        // Crear el contenedor de noticias
        Table newsGrid = new Table();
        newsGrid.top().padTop(10);

        // Obtener las noticias desde NewsManager
        List<NewsManager.NewsData> newsList = NewsManager.getAllNews();
        Collections.reverse(newsList); // Invertir el orden de la lista para mostrar las más recientes primero


        // Mostrar noticias en cuadrícula
        int columns = 3; // Número de columnas en la cuadrícula
        int counter = 0;

        for (NewsManager.NewsData news : newsList) {
            Table newsCard = createNewsCard(news);
            newsGrid.add(newsCard).pad(10).size(300, 300); // Tamaño de cada celda

            counter++;
            if (counter % columns == 0) {
                newsGrid.row(); // Nueva fila después de cada fila completa
            }
        }

        // ScrollPane para la cuadrícula de noticias
        ScrollPane scrollPane = new ScrollPane(newsGrid);
        scrollPane.setScrollingDisabled(true, false); // Permitir solo desplazamiento vertical
        scrollPane.setFadeScrollBars(false); // Mostrar barras de desplazamiento siempre visibles
        scrollPane.setScrollbarsVisible(true); // Mostrar barras visibles

        mainTable.add(scrollPane).fill().expand();

        stage.addActor(mainTable);
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 110);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminMenuScreen(username));
            }
        });
        stage.addActor(backArrowButton);

    }

    private Table createNewsCard(NewsManager.NewsData news) {
        Table card = new Table();

        // Fondo gris para cada tarjeta
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.8f, 0.8f, 0.8f, 0.8f));
        pixmap.fill();
        TextureRegionDrawable background = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
        card.setBackground(background);


        // Procesar título para que sea más corto si es necesario
        String title = news.getName();
        int maxLength = 30; // Longitud máxima del título
        if (title.length() > maxLength) {
            title = title.substring(0, maxLength - 3) + "..."; // Recortar y agregar "..."
        }

        // Título de la noticia
        Label newsTitleLabel = new Label(title, new Label.LabelStyle(font, Color.BLACK));
        newsTitleLabel.setWrap(true);
        card.add(newsTitleLabel).width(280).padTop(5).row();

        // Añadir listener para ir a AdminNewsDetailScreen
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminNewsDetailScreen(username, news));
            }
        });

        return card;
    }


    private Texture createBackground(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
        if (backArrowButton != null) {
            backArrowButton.setPosition(10, height - 150);
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

    private Pixmap createRoundedPixmap(int width, int height, Color color, float transparency) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);

        // Establecer el color con la transparencia
        Color transparentColor = new Color(color.r, color.g, color.b, transparency);
        pixmap.setColor(transparentColor);

        pixmap.fillRectangle(0, 20, width, height - 2 * 20); // Centro vertical

        return pixmap;
    }
}
