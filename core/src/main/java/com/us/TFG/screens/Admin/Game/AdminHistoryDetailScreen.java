// Clase completa AdminHistoryDetailScreen con el dialogo de publicar/despublicar separado
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.*;
import com.us.TFG.model.*;
import com.us.TFG.screens.Admin.AdminForumScreen;
import com.us.TFG.utils.*;

import java.util.List;

public class AdminHistoryDetailScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;

    private String username;
    private History history;
    private List<Chapter> chapters;
    private ImageButton backArrowButton;

    public AdminHistoryDetailScreen(String username, History history) {
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

        chapters = ChapterManager.getChaptersByHistoryId(history.getId());

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(50);
        stage.addActor(mainTable);

        Pixmap backgroundPixmap = UIStyle.createRoundedPixmap(500, 600, new Color(0.6f, 0.3f, 0.6f, 0.8f), 0.8f);
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new Texture(backgroundPixmap));
        backgroundPixmap.dispose();

        Table leftTable = new Table();
        leftTable.setBackground(backgroundDrawable);

        Label titleTag = new Label("Título:", UIStyle.getLabelStyle());
        Label titleLabel = new Label(history.getTitle(), UIStyle.getTitleLabelStyle());

        Label descTag = new Label("Descripción:", UIStyle.getLabelStyle());
        Label descLabel = new Label(history.getDescription(), UIStyle.getLabelStyle());
        descLabel.setWrap(true);

        Table descTable = new Table();
        descTable.add(descLabel).width(380).left().top();

        ScrollPane.ScrollPaneStyle descScrollStyle = new ScrollPane.ScrollPaneStyle();
        descScrollStyle.vScroll = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(6, 200, new Color(0.3f, 0.1f, 0.4f, 0.3f), 1f))
        );
        descScrollStyle.vScrollKnob = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(6, 60, new Color(0.9f, 0.7f, 1f, 1f), 1f))
        );

        ScrollPane descScroll = new ScrollPane(descTable, descScrollStyle);
        descScroll.setFadeScrollBars(false);
        descScroll.setScrollingDisabled(true, false);
        descScroll.setScrollbarsOnTop(false);
        descScroll.setForceScroll(false, true);

        TextButton editarBtn = new TextButton("Editar", UIStyle.getButtonStyle());
        editarBtn.setDisabled(history.isPublished());
        editarBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showEditHistoryDialog(history);
                }
            });

        TextButton publicarBtn = new TextButton(history.isPublished() ? "Despublicar" : "Publicar", UIStyle.getButtonStyle());
        publicarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPublishConfirmDialog();
            }
        });

        TextButton eliminarBtn = new TextButton("Borrar historia", UIStyle.getButtonStyle());
        eliminarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDeleteConfirmDialog();
            }
        });


        leftTable.add(titleTag).left().padTop(15).row();
        leftTable.add(titleLabel).left().padBottom(20).row();
        leftTable.add(descTag).left().padBottom(5).row();
        leftTable.add(descScroll).width(400).height(150).left().padBottom(30).row();
        if (!history.isPublished()) {
            leftTable.add(editarBtn).left().row();
            leftTable.add(eliminarBtn).left().row();

        }
        leftTable.add(publicarBtn).left().padTop(5).padBottom(10);

        Table rightTable = new Table();
        rightTable.setBackground(backgroundDrawable);

        Label chaptersTitle = new Label("Capítulos", UIStyle.getTitleLabelStyle());
        rightTable.add(chaptersTitle).left().padBottom(20).padTop(10).row();

        Table chaptersListTable = new Table().top().left();
        if (chapters.isEmpty()) {
            Label noChaptersLabel = new Label("No hay capítulos", UIStyle.getLabelStyle());
            chaptersListTable.add(noChaptersLabel).left().padBottom(10).row();
        } else {
            for (Chapter chapter : chapters) {
                Table chapterRow = new Table();

                Label chapterLabel = new Label("- " + chapter.getName() + " " + chapter.getChapterNumber(), UIStyle.getLabelStyle());
                TextButton editButton = new TextButton("Editar", UIStyle.getButtonStyle());

                    editButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            Render.app.setScreen(new AdminEditChapterScreen(username, chapter,history));
                        }
                    });


                chapterRow.add(chapterLabel).left().padRight(10);
                if (!history.isPublished()) {
                    chapterRow.add(editButton).width(100).right();
                }
                chaptersListTable.add(chapterRow).left().padBottom(10).row();
            }
        }

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(6, 200, new Color(0.3f, 0.1f, 0.4f, 0.3f), 1f))
        );
        scrollStyle.vScrollKnob = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(6, 60, new Color(0.9f, 0.7f, 1f, 1f), 1f))
        );

        ScrollPane scrollPane = new ScrollPane(chaptersListTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setForceScroll(false, true);

        rightTable.add(scrollPane).width(400).height(300).expandY().fillY();

        TextButton crearCapituloButton = new TextButton("+ Crear Capítulo", UIStyle.getButtonStyle());
        crearCapituloButton.setDisabled(history.isPublished());
            crearCapituloButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Render.app.setScreen(new AdminCreateChapterScreen(username, history));
                }
            });

        rightTable.row().padTop(20);
        if (!history.isPublished()) {
            rightTable.add(crearCapituloButton).width(200).center().padBottom(10);
        }

        mainTable.add(leftTable).left().padRight(50).width(500).top();
        mainTable.add(rightTable).left().width(500).top();

        // Crear la flecha de retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminGameScreen(username));
            }
        });
        stage.addActor(backArrowButton);
    }

    private void showPublishConfirmDialog() {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 300, new Color(0.6f, 0.3f, 0.6f, 1f), 1f))
        );

        Dialog publishDialog = new Dialog(history.isPublished() ? "Despublicar historia" : "¿Publicar historia?", dialogStyle);

        String message = history.isPublished()
            ? "¿Estás seguro? Si despublicas la historia, la gente no podrá jugarla."
            : "¿Estás seguro? Una vez publicada no se podrá editar.";

        Label confirmLabel = new Label(message, UIStyle.getLabelStyle());
        confirmLabel.setWrap(true);
        confirmLabel.setAlignment(Align.center);

        publishDialog.getContentTable().add(confirmLabel).width(400).pad(20).row();

        TextButton confirmBtn = new TextButton("Sí", UIStyle.getButtonStyle());
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean nuevoEstado = !history.isPublished();
                history.setPublished(nuevoEstado);
                HistoryManager.setPublished(history.getId(), nuevoEstado);
                publishDialog.hide();
                publishDialog.remove();
                Render.app.setScreen(new AdminHistoryDetailScreen(username, history));
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                publishDialog.hide();
                publishDialog.remove();
            }
        });

        publishDialog.getButtonTable().add(confirmBtn).pad(10);
        publishDialog.getButtonTable().add(cancelBtn).pad(10);

        publishDialog.show(stage);
    }


    private void showDeleteConfirmDialog() {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 300, new Color(0.6f, 0.2f, 0.2f, 1f), 1f))
        );

        Dialog confirmDialog = new Dialog("¿Eliminar historia?", dialogStyle);

        Label warning = new Label("¿Estás seguro de que quieres eliminar esta historia?\nEsta acción no se puede deshacer.", UIStyle.getLabelStyle());
        warning.setWrap(true);
        warning.setAlignment(Align.center);

        confirmDialog.getContentTable().add(warning).width(400).pad(20).row();

        TextButton confirmBtn = new TextButton("Sí, eliminar", UIStyle.getButtonStyle());
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Obtener capítulos de la historia
                List<Chapter> chapters = ChapterManager.getChaptersByHistoryId(history.getId());

                for (Chapter chapter : chapters) {
                    // Obtener escenas del capítulo
                    List<Scene> scenes = SceneManager.getScenesByChapterId(chapter.getId());

                    for (Scene scene : scenes) {
                        // Obtener decisiones de la escena
                        List<Decision> decisions = DecisionManager.getDecisionsBySceneId(scene.getId());

                        for (Decision decision : decisions) {
                            // Obtener opciones de la decisión
                            List<Option> options = OptionManager.getOptionsByDecisionId(decision.getId());

                            for (Option option : options) {
                                OptionManager.deleteOption(option.getId()); // Eliminar opción
                            }

                            DecisionManager.deleteDecision(decision.getId()); // Eliminar decisión
                        }

                        SceneManager.deleteScene(scene.getId()); // Eliminar escena
                    }

                    ChapterManager.deleteChapter(chapter.getId()); // Eliminar capítulo
                }
                HistoryManager.deleteHistory(history.getId());
                confirmDialog.hide();
                Render.app.setScreen(new AdminGameScreen(username));
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

    @Override
    public void render(float delta) {
        batch.begin();
        fondo.draw();
        batch.end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        topBar.updateSize();
        if (backArrowButton != null) {
            backArrowButton.setPosition(10, height - 150);
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }

    private void showEditHistoryDialog(History history) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(1f, 0f, 0.5f, 1f), 1f))
        );

        Dialog editDialog = new Dialog("Editar Historia", dialogStyle);
        TextField titleField = new TextField(history.getTitle(), UIStyle.getTextFieldStyle());
        TextArea descArea = new TextArea(history.getDescription(), UIStyle.getTextFieldStyle());

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        editDialog.getContentTable().add(new Label("Título:", labelStyle)).left().pad(5);
        editDialog.getContentTable().add(titleField).width(350).pad(5).row();
        editDialog.getContentTable().add(new Label("Descripción:", labelStyle)).left().pad(5);
        editDialog.getContentTable().add(descArea).width(350).height(200).row();

        Label feedbackLabel = new Label("", UIStyle.getLabelStyle());
        feedbackLabel.setWrap(true);
        feedbackLabel.setAlignment(Align.center);

        TextButton saveButton = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String newTitle = titleField.getText().trim();
                String newDesc = descArea.getText().trim();

                if (newTitle.isEmpty() || newDesc.isEmpty()) {
                    feedbackLabel.setText("El título y la descripción no pueden estar vacíos.");
                    feedbackLabel.setColor(Color.RED);
                    return;
                }

                HistoryManager.updateHistory(history.getId(), newTitle, newDesc);
                history.setTitle(newTitle);
                history.setDescription(newDesc);

                feedbackLabel.setText("Historia actualizada con éxito.");
                feedbackLabel.setColor(Color.GREEN);

                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override
                    public void run() {
                        editDialog.hide();
                        editDialog.remove();
                        Render.app.setScreen(new AdminHistoryDetailScreen(username, history));
                    }
                }, 2);
            }
        });

        TextButton cancelButton = new TextButton("Cerrar", UIStyle.getButtonStyle());
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editDialog.hide();
                editDialog.remove();
            }
        });

        editDialog.getButtonTable().add(saveButton).pad(10);
        editDialog.getButtonTable().add(cancelButton).pad(10);
        editDialog.getContentTable().add(feedbackLabel).colspan(2).padTop(10).width(400).row();
        editDialog.show(stage);
    }
}
