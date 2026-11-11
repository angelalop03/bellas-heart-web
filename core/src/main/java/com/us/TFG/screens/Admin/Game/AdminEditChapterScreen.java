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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.*;
import com.us.TFG.model.*;
import com.us.TFG.utils.*;

import java.util.ArrayList;
import java.util.List;

public class AdminEditChapterScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private Chapter chapter;
    private History history;
    private ImageButton backArrowButton;

    public AdminEditChapterScreen(String username, Chapter chapter, History history) {
        this.username = username;
        this.chapter = chapter;
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
        mainTable.pad(50);
        stage.addActor(mainTable);

        Pixmap backgroundPixmap = UIStyle.createRoundedPixmap(500, 600, new Color(0.6f, 0.3f, 0.6f, 0.8f), 0.8f);
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new Texture(backgroundPixmap));
        backgroundPixmap.dispose();

        Table leftTable = new Table();
        leftTable.setBackground(backgroundDrawable);

        leftTable.add(new Label("Nombre:", UIStyle.getLabelStyle())).left().padBottom(5).row();
        leftTable.add(new Label(chapter.getName(), UIStyle.getTitleLabelStyle())).left().padBottom(20).row();

        leftTable.add(new Label("Número de Capítulo:", UIStyle.getLabelStyle())).left().padBottom(5).row();
        leftTable.add(new Label(chapter.getChapterNumber(), UIStyle.getLabelStyle())).left().padBottom(20).row();

        leftTable.add(new Label("Estado de Relación Requerido:", UIStyle.getLabelStyle())).left().padBottom(5).row();
        String state = chapter.getRequiredRelationshipState() != null ? chapter.getRequiredRelationshipState() : "Ninguno";
        leftTable.add(new Label(state, UIStyle.getLabelStyle())).left().padBottom(20).row();

        TextButton editBtn = new TextButton("Editar Capítulo", UIStyle.getButtonStyle());
        editBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showEditChapterDialog(chapter);
            }
        });

        TextButton eliminarBtn = new TextButton("Eliminar Capítulo", UIStyle.getButtonStyle());
        eliminarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDeleteChapterDialog();
            }
        });
        leftTable.add(eliminarBtn).left().padTop(10).row();


        leftTable.add(editBtn).left().padTop(20);

        Table rightTable = new Table();
        rightTable.setBackground(backgroundDrawable);

        rightTable.add(new Label("Escenas", UIStyle.getTitleLabelStyle())).left().padBottom(20).padTop(20).row();

        List<Scene> scenes = SceneManager.getScenesByChapterId(chapter.getId());
        Table sceneListTable = new Table().top().left();

        if (scenes.isEmpty()) {
            sceneListTable.add(new Label("No hay escenas", UIStyle.getLabelStyle())).left().padBottom(10).row();
        } else {
            for (Scene scene : scenes) {
                Table sceneEntry = new Table();
                sceneEntry.add(new Label("Escena " + scene.getId(), UIStyle.getLabelStyle())).left().padRight(10);

                TextButton editSceneBtn = new TextButton("Editar", UIStyle.getButtonStyle());
                editSceneBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        showEditSceneDialog(scene);
                    }
                });

                TextButton manageBtn = new TextButton("Gestionar", UIStyle.getButtonStyle());
                manageBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Render.app.setScreen(new AdminVisualStoryEditorScreen(username,scene,history));
                    }
                });

                sceneEntry.add(manageBtn).width(100).right();
                sceneEntry.add(editSceneBtn).width(100).left().pad(10);

                sceneListTable.add(sceneEntry).left().padBottom(10).row();
            }
        }

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(6, 200, new Color(0.3f, 0.1f, 0.4f, 0.3f), 1f))
        );
        scrollStyle.vScrollKnob = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(6, 60, new Color(0.9f, 0.7f, 1f, 1f), 1f))
        );

        ScrollPane scrollPane = new ScrollPane(sceneListTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setForceScroll(false, true);

        rightTable.add(scrollPane).width(400).height(300).expandY().fillY().row();

        TextButton createSceneButton = new TextButton("Crear Escena", UIStyle.getButtonStyle());
        createSceneButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminCreateSceneScreen(username, chapter,history));
            }
        });

        rightTable.add(createSceneButton).width(300).padTop(20).center().padBottom(20).row();

        mainTable.add(leftTable).left().padRight(50).width(500).top();
        mainTable.add(rightTable).left().width(500).top();

        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminHistoryDetailScreen(username, HistoryManager.getHistoryByChapterId(chapter.getId())));
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

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true);
        topBar.updateSize();
        if (backArrowButton != null) {
            backArrowButton.setPosition(10, height - 150);
        }}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }

    private void showEditChapterDialog(Chapter chapter) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(1f, 0f, 0.5f, 1f), 1f))
        );

        Dialog editDialog = new Dialog("Editar Capítulo", dialogStyle);

        TextField nameField = new TextField(chapter.getName(), UIStyle.getTextFieldStyle());
        TextField numberField = new TextField(chapter.getChapterNumber(), UIStyle.getTextFieldStyle());

        SelectBox<String> relationshipBox = new SelectBox<>(UIStyle.getSelectBoxStyle());
        relationshipBox.setItems("NULL", "CONFIDENT", "LOW_TENSION", "HIGH_TENSION", "CRITICAL");
        relationshipBox.setSelected(chapter.getRequiredRelationshipState() == null ? "NULL" : chapter.getRequiredRelationshipState());

        Label feedback = new Label("", UIStyle.getLabelStyle());
        feedback.setWrap(true);

        editDialog.getContentTable().add(new Label("Nombre:", UIStyle.getLabelStyle())).left().pad(5);
        editDialog.getContentTable().add(nameField).width(350).pad(5).row();

        editDialog.getContentTable().add(new Label("Número:", UIStyle.getLabelStyle())).left().pad(5);
        editDialog.getContentTable().add(numberField).width(350).pad(5).row();

        editDialog.getContentTable().add(new Label("Relación:", UIStyle.getLabelStyle())).left().pad(5);
        editDialog.getContentTable().add(relationshipBox).width(350).pad(5).row();

        TextButton saveBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String newName = nameField.getText().trim();
                String newNumber = numberField.getText().trim();
                String newRelation = relationshipBox.getSelected();
                String relationToSave = newRelation.equals("NULL") ? null : newRelation;

                if (newName.isEmpty() || newNumber.isEmpty()) {
                    UIStyle.showError(feedback, "Todos los campos son obligatorios.");
                    return;
                }

                List<Chapter> existingChapters = ChapterManager.getChaptersByHistoryId(chapter.getHistory());
                for (Chapter c : existingChapters) {
                    if (c.getId() != chapter.getId() && c.getChapterNumber().equalsIgnoreCase(newNumber)) {
                        UIStyle.showError(feedback, "Ya existe un capítulo con ese número.");
                        return;
                    }
                }

                chapter.setName(newName);
                chapter.setChapterNumber(newNumber);
                chapter.setRequiredRelationshipState(relationToSave);

                ChapterManager.updateChapter(chapter);

                feedback.setText("Capítulo actualizado.");
                feedback.setColor(Color.GREEN);

                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override
                    public void run() {
                        editDialog.hide();
                        editDialog.remove();
                        Render.app.setScreen(new AdminEditChapterScreen(username, chapter,history));
                    }
                }, 2);
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editDialog.hide();
            }
        });

        editDialog.getButtonTable().add(saveBtn).pad(10);
        editDialog.getButtonTable().add(cancelBtn).pad(10);
        editDialog.getContentTable().add(feedback).colspan(2).width(400).padTop(10).row();

        editDialog.show(stage);
    }

    private void showDeleteChapterDialog() {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 300, new Color(0.6f, 0.2f, 0.2f, 1f), 1f))
        );

        Dialog confirmDialog = new Dialog("¿Eliminar capítulo?", dialogStyle);

        Label warning = new Label("¿Estás seguro de que quieres eliminar este capítulo?\nSe eliminarán también todas sus escenas.", UIStyle.getLabelStyle());
        warning.setWrap(true);
        warning.setAlignment(Align.center);

        confirmDialog.getContentTable().add(warning).width(400).pad(20).row();

        TextButton confirmBtn = new TextButton("Sí, eliminar", UIStyle.getButtonStyle());
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ChapterManager.deleteChapter(chapter.getId()); // Asegúrate de que elimina escenas también
                confirmDialog.hide();
                Render.app.setScreen(new AdminHistoryDetailScreen(username, history));
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.hide();
            }
        });

        confirmDialog.getButtonTable().add(confirmBtn).pad(10);
        confirmDialog.getButtonTable().add(cancelBtn).pad(10);
        confirmDialog.show(stage);
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

    private void updatePreviewImage(Image previewImage, String selectedImage) {
        if (selectedImage != null && Gdx.files.internal("photos/Scenes/" + selectedImage).exists()) {
            previewImage.setDrawable(new TextureRegionDrawable(
                new Texture(Gdx.files.internal("photos/Scenes/" + selectedImage))
            ));
        }
    }

    private void showEditSceneDialog(Scene scene) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(0.3f, 0.2f, 0.5f, 1f), 1f))
        );

        Dialog dialog = new Dialog("Editar Escena", dialogStyle);

        SelectBox<String> backgroundImageBox = new SelectBox<>(UIStyle.getSelectBoxStyle());
        backgroundImageBox.setItems(getSceneImageList());
        backgroundImageBox.setSelected(scene.getBackgroundImage());

        Image previewImage = new Image();
        updatePreviewImage(previewImage, backgroundImageBox.getSelected());

        backgroundImageBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updatePreviewImage(previewImage, backgroundImageBox.getSelected());
            }
        });

        backgroundImageBox.addListener(new ClickListener() {
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updatePreviewImage(previewImage, backgroundImageBox.getSelected());
            }
        });

        Label feedback = new Label("", UIStyle.getLabelStyle());

        TextButton saveButton = new TextButton("Guardar Cambios", UIStyle.getButtonStyle());
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scene.setBackgroundImage(backgroundImageBox.getSelected());
                SceneManager.updateScene(scene);
                dialog.hide();
            }
        });

        TextButton deleteButton = new TextButton("Eliminar Escena", UIStyle.getButtonStyle());
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                showDeleteSceneDialog(scene);
            }
        });

        TextButton cancelButton = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getContentTable().add(new Label("Imagen de Fondo:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(backgroundImageBox).width(300).pad(5).row();
        dialog.getContentTable().add(new Label("Previsualización:", UIStyle.getLabelStyle())).left().padTop(10).colspan(2).row();
        dialog.getContentTable().add(previewImage).width(300).height(150).colspan(2).pad(10).row();

        dialog.getButtonTable().add(saveButton).pad(10);
        dialog.getButtonTable().add(deleteButton).pad(10);
        dialog.getButtonTable().add(cancelButton).pad(10);

        dialog.show(stage);
    }

    private void showDeleteSceneDialog(Scene scene) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 300, new Color(0.6f, 0.2f, 0.2f, 1f), 1f))
        );

        Dialog confirmDialog = new Dialog("¿Eliminar escena?", dialogStyle);
        Label warning = new Label("¿Estás seguro de que quieres eliminar esta escena?", UIStyle.getLabelStyle());
        warning.setWrap(true);
        warning.setAlignment(Align.center);

        confirmDialog.getContentTable().add(warning).width(400).pad(20).row();

        TextButton confirmBtn = new TextButton("Sí, eliminar", UIStyle.getButtonStyle());
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                List<Decision> decisions = DecisionManager.getDecisionsBySceneId(scene.getId());

                for (Decision decision : decisions) {
                    // Obtener y eliminar todas las opciones de cada decisión
                    List<Option> options = OptionManager.getOptionsByDecisionId(decision.getId());
                    for (Option option : options) {
                        OptionManager.deleteOption(option.getId());
                    }

                    // Eliminar la decisión
                    DecisionManager.deleteDecision(decision.getId());
                }

                // Finalmente eliminar la escena
                SceneManager.deleteScene(scene.getId());
                confirmDialog.hide();
                Render.app.setScreen(new AdminEditChapterScreen(username, chapter, history));
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.hide();
            }
        });

        confirmDialog.getButtonTable().add(confirmBtn).pad(10);
        confirmDialog.getButtonTable().add(cancelBtn).pad(10);
        confirmDialog.show(stage);
    }
}



