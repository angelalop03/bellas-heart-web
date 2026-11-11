package com.us.TFG.screens.Admin.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.SceneManager;
import com.us.TFG.model.Chapter;
import com.us.TFG.model.History;
import com.us.TFG.model.Scene;
import com.us.TFG.utils.*;

import java.util.ArrayList;
import java.util.List;

public class AdminCreateSceneScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private Chapter chapter;

    private SelectBox<String> backgroundImageBox;
    private TextField orderField;
    private Image previewImage;
    private Label feedbackLabel;
    private History history;

    public AdminCreateSceneScreen(String username, Chapter chapter, History history) {
        this.username = username;
        this.chapter = chapter;
        this.history =history;
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

        Label title = new Label("Crear Escena", UIStyle.getTitleLabelStyle());
        formTable.add(title).colspan(2).padBottom(30).row();

        formTable.add(new Label("Imagen de Fondo:", UIStyle.getLabelStyle())).left().pad(10);
        backgroundImageBox = new SelectBox<>(UIStyle.getSelectBoxStyle());
        backgroundImageBox.setItems(getSceneImageList());
        formTable.add(backgroundImageBox).width(200).pad(10).row();

        previewImage = new Image();
        formTable.add(previewImage).colspan(2).width(300).height(150).pad(10).row();

        backgroundImageBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updatePreview();
            }
        });
        backgroundImageBox.addListener(new ClickListener() {
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updatePreview();
            }
        });

        formTable.add(new Label("Orden de la Escena:", UIStyle.getLabelStyle())).left().pad(10);

        // Prellenar automáticamente con el siguiente orden
        int siguienteOrden = getNextSceneOrder();
        orderField = new TextField(String.valueOf(siguienteOrden), UIStyle.getTextFieldStyle());
        formTable.add(orderField).width(400).pad(10).row();

        feedbackLabel = new Label("", UIStyle.getLabelStyle());
        formTable.add(feedbackLabel).colspan(2).pad(10).row();

        TextButton guardarBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        TextButton cancelarBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());

        guardarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                crearEscena();
            }
        });

        cancelarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminEditChapterScreen(username, chapter,history));
            }
        });

        Table buttons = new Table();
        buttons.add(guardarBtn).padRight(20);
        buttons.add(cancelarBtn);
        formTable.add(buttons).colspan(2).padTop(20);

        mainTable.add(formTable).center();

        updatePreview();
    }

    private void updatePreview() {
        String selected = backgroundImageBox.getSelected();
        if (selected != null && Gdx.files.internal("photos/Scenes/" + selected).exists()) {
            previewImage.setDrawable(new TextureRegionDrawable(new Texture(Gdx.files.internal("photos/Scenes/" + selected))));
        }
    }

    private void crearEscena() {
        String selectedImage = backgroundImageBox.getSelected();
        String orderText = orderField.getText().trim();

        if (orderText.isEmpty() || !orderText.matches("\\d+")) {
            UIStyle.showError(feedbackLabel, "El orden debe ser un número válido.");
            return;
        }

        int order = Integer.parseInt(orderText);

        Scene newScene = new Scene();
        newScene.setChapterId(chapter.getId());
        newScene.setBackgroundImage(selectedImage);
        newScene.setSceneOrder(order);

        int sceneId = SceneManager.createScene(newScene);
        if (sceneId > 0) {
            Render.app.setScreen(new AdminEditChapterScreen(username, chapter,history));
        } else {
            UIStyle.showError(feedbackLabel, "Error al crear la escena.");
        }
    }

    private int getNextSceneOrder() {
        List<Scene> escenas = SceneManager.getScenesByChapterId(chapter.getId());
        if (escenas == null || escenas.isEmpty()) {
            return 1;
        }

        int maxOrder = 0;
        for (Scene escena : escenas) {
            if (escena.getSceneOrder() > maxOrder) {
                maxOrder = escena.getSceneOrder();
            }
        }
        return maxOrder + 1;
    }

    private String[] getSceneImageList() {
        FileHandle dirHandle = Gdx.files.internal("photos/Scenes");
        if (!dirHandle.exists() || !dirHandle.isDirectory()) return new String[]{"Default_Scene.png"};

        FileHandle[] files = dirHandle.list();
        List<String> imageNames = new ArrayList<>();
        for (FileHandle file : files) {
            if (file.name().endsWith(".png") || file.name().endsWith(".jpg")) {
                imageNames.add(file.name());
            }
        }

        return imageNames.toArray(new String[0]);
    }

    @Override public void render(float delta) {
        batch.begin();
        fondo.draw();
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        topBar.updateSize();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
