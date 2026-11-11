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
import com.us.TFG.utils.*;

public class AdminNewsDetailScreen implements Screen {

    private Stage stage;
    private SpriteBatch b;
    private Imagen fondo;
    private BitmapFont font;
    private BitmapFont titleFont;
    private NewsManager.NewsData news;
    private Skin skin;
    private TopBar topBar;
    private String username;
    private ImageButton backArrowButton;

    public AdminNewsDetailScreen(String username, NewsManager.NewsData news) {
        this.username = username;
        this.news = news;
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
        font = UIStyle.getFont();
        skin = new Skin();
        // Añadir la fuente generada al skin
        skin.add("Caveat", font);

        // Crear la barra superior
        topBar = new TopBar(username, stage, font);

        // Crear la flecha de retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100); // Tamaño de la flecha
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150); // Posición en la esquina superior izquierda
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminForumScreen(username)); // Volver a AdminForumScreen
            }
        });

        // Crear contenedor principal
        Table contentTable = new Table();
        contentTable.top();

        // Fondo grisáceo
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.8f, 0.8f, 0.8f, 0.8f));
        pixmap.fill();
        contentTable.setBackground(new TextureRegionDrawable(new Texture(pixmap)));
        pixmap.dispose();

        // Botones "Editar" y "Eliminar"
        Table buttonTable = new Table();
        TextButton.TextButtonStyle buttonStyle = UIStyle.getButtonStyle();

        TextButton editButton = new TextButton("Editar", buttonStyle);
        TextButton deleteButton = new TextButton("Eliminar", buttonStyle);

        // Listeners para los botones
        editButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Acción para editar la noticia
                Render.app.setScreen(new AdminEditNewsScreen(username, news));
            }
        });

        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDeleteDialog();
            }
        });

        buttonTable.add(editButton).pad(10).width(200).height(50);
        buttonTable.add(deleteButton).pad(10).width(200).height(50);
        contentTable.add(buttonTable).padTop(20).center().row();


        // Mostrar título
        Label newsTitleLabel = new Label(news.getName(), new Label.LabelStyle(UIStyle.getFont(), Color.DARK_GRAY));
        newsTitleLabel.setWrap(true);
        newsTitleLabel.setAlignment(1);
        contentTable.add(newsTitleLabel).width(Gdx.graphics.getWidth() * 0.9f).center().pad(10).row();

        // Mostrar contenido
        Label contentLabel = new Label(news.getText(), new Label.LabelStyle(font, Color.DARK_GRAY));
        contentLabel.setWrap(true);
        contentTable.add(contentLabel).width(Gdx.graphics.getWidth() * 0.9f).center().pad(20).row();

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.add(scrollPane).expand().fill().padTop(60);

        stage.addActor(mainTable);
        stage.addActor(backArrowButton);
    }

    private void showDeleteDialog() {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(400, 300, Color.RED, 1f)));

        Dialog deleteDialog = new Dialog("", dialogStyle);


        deleteDialog.text(new Label("¿Estás seguro de eliminar esta noticia?", UIStyle.getLabelStyle())).pad(10);


        TextButton confirmButton = new TextButton("Confirmar", UIStyle.getButtonStyle());
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (NewsManager.deleteNews(news.getId())) {
                    deleteDialog.hide();
                    deleteDialog.remove();
                    Render.app.setScreen(new AdminForumScreen(username));
                } else {
                    deleteDialog.text(new Label("Error al eliminar la noticia.", UIStyle.getLabelStyle())).pad(10);
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



    @Override
    public void render(float delta) {
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
}
