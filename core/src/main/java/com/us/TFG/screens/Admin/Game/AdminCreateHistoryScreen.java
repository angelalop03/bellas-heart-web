package com.us.TFG.screens.Admin.Game;

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
import com.us.TFG.model.History;
import com.us.TFG.utils.*;

public class AdminCreateHistoryScreen implements Screen {

    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;

    private TextField titleField;
    private TextArea descriptionArea;
    private Label feedbackLabel;

    public AdminCreateHistoryScreen(String username) {
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

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Pixmap pixmap = new Pixmap(1000, 600, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.5f, 0.8f));
        pixmap.fill();
        TextureRegionDrawable background = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        Table formTable = new Table();
        formTable.setBackground(background);

        // Título de pantalla
        Label titleLabel = new Label("Crear Nueva Historia", UIStyle.getTitleLabelStyle());
        formTable.add(titleLabel).colspan(2).padBottom(30);
        formTable.row();

        // Campo título
        formTable.add(new Label("Título:", UIStyle.getLabelStyle())).left().pad(10);
        titleField = new TextField("", UIStyle.getTextFieldStyle());
        formTable.add(titleField).width(400).pad(10);
        formTable.row();

        // Campo descripción
        formTable.add(new Label("Descripción:", UIStyle.getLabelStyle())).left().pad(10);
        descriptionArea = new TextArea("", UIStyle.getTextFieldStyle());
        formTable.add(descriptionArea).width(400).height(100).pad(10);
        formTable.row();

        // Feedback
        feedbackLabel = new Label("", UIStyle.getLabelStyle());
        formTable.add(feedbackLabel).colspan(2).pad(10);
        formTable.row();

        // Botones
        TextButton guardarBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        TextButton cancelarBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());

        guardarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                crearHistoria();
            }
        });

        cancelarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminGameScreen(username));
            }
        });

        Table buttonsTable = new Table();
        buttonsTable.add(guardarBtn).padRight(20);
        buttonsTable.add(cancelarBtn);
        formTable.add(buttonsTable).colspan(2).padTop(20);

        mainTable.add(formTable).center();
    }

    private void crearHistoria() {
        String titulo = titleField.getText().trim();
        String descripcion = descriptionArea.getText().trim();

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            UIStyle.showError(feedbackLabel, "Todos los campos son obligatorios.");
            return;
        }

        History historia = new History();
        historia.setTitle(titulo);
        historia.setImagePath("Default_History.png"); // Imagen por defecto
        historia.setDescription(descripcion);
        historia.setPublished(false);

        boolean resultado = HistoryManager.createHistory(historia);

        if (resultado) {
            Render.app.setScreen(new AdminGameScreen(username));
        } else {
            UIStyle.showError(feedbackLabel, "Error al guardar la historia.");
        }
    }

    @Override
    public void render(float delta) {
        batch.begin();
        fondo.draw();
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true);
        topBar.updateSize();}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}
