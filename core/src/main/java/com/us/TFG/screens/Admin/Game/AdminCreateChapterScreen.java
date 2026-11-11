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
import com.us.TFG.manager.ChapterManager;
import com.us.TFG.model.Chapter;
import com.us.TFG.model.History;
import com.us.TFG.model.RelationshipState;
import com.us.TFG.utils.*;

import java.util.List;

public class AdminCreateChapterScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private History history;

    private TextField nameField, chapterNumberField;
    private SelectBox<String> relationshipBox;
    private Label feedbackLabel;

    public AdminCreateChapterScreen(String username, History history) {
        this.username = username;
        this.history = history;
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

        Label title = new Label("Crear Capítulo", UIStyle.getTitleLabelStyle());
        formTable.add(title).colspan(2).padBottom(30);
        formTable.row();

        formTable.add(new Label("Nombre:", UIStyle.getLabelStyle())).left().pad(10);
        nameField = new TextField("", UIStyle.getTextFieldStyle());
        formTable.add(nameField).width(400).pad(10);
        formTable.row();

        formTable.add(new Label("Número de Capítulo:", UIStyle.getLabelStyle())).left().pad(10);
        chapterNumberField = new TextField("", UIStyle.getTextFieldStyle());
        formTable.add(chapterNumberField).width(400).pad(10);
        formTable.row();

        formTable.add(new Label("Relación requerida:", UIStyle.getLabelStyle())).left().pad(10);
        relationshipBox = new SelectBox<>(UIStyle.getSelectBoxStyle());
        relationshipBox.setItems("NULL", "CONFIDENT", "LOW_TENSION", "HIGH_TENSION", "CRITICAL");
        formTable.add(relationshipBox).width(400).pad(10);
        formTable.row();


        feedbackLabel = new Label("", UIStyle.getLabelStyle());
        formTable.add(feedbackLabel).colspan(2).pad(10);
        formTable.row();

        TextButton guardarBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        TextButton cancelarBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());

        guardarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                crearCapitulo();
            }
        });

        cancelarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminHistoryDetailScreen(username, history));
            }
        });

        Table buttons = new Table();
        buttons.add(guardarBtn).padRight(20);
        buttons.add(cancelarBtn);
        formTable.add(buttons).colspan(2).padTop(20);

        mainTable.add(formTable).center();
    }

    private void crearCapitulo() {
        String name = nameField.getText().trim();
        String chapterNumber = chapterNumberField.getText().trim();
        String relationshipSelection = relationshipBox.getSelected();

        if (name.isEmpty() || chapterNumber.isEmpty()) {
            UIStyle.showError(feedbackLabel, "Todos los campos son obligatorios.");
            return;
        }

        List<Chapter> existingChapters = ChapterManager.getChaptersByHistoryId(history.getId());
        for (Chapter chapter : existingChapters) {
            if (chapter.getChapterNumber().equalsIgnoreCase(chapterNumber)) {
                UIStyle.showError(feedbackLabel, "Ya existe un capítulo con ese número.");
                return;
            }
        }

        Chapter newChapter = new Chapter();
        newChapter.setName(name);
        newChapter.setChapterNumber(chapterNumber);
        newChapter.setRequiredRelationshipState(
            "NULL".equalsIgnoreCase(relationshipSelection) ? null : relationshipSelection
        );        newChapter.setHistory(history.getId());
        newChapter.setImagePath("Default_Chapter.png"); // Imagen por defecto

        boolean success = ChapterManager.createChapter(newChapter);

        if (success) {
            Render.app.setScreen(new AdminHistoryDetailScreen(username, history));
        } else {
            UIStyle.showError(feedbackLabel, "Error al guardar el capítulo.");
        }
    }

    @Override public void render(float delta) {
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
