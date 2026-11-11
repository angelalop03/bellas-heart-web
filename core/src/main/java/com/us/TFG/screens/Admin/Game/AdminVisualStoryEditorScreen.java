package com.us.TFG.screens.Admin.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.manager.*;
import com.us.TFG.model.*;
import com.us.TFG.utils.*;

import java.util.*;
import java.util.List;

public class AdminVisualStoryEditorScreen implements Screen {
    private String username;
    private Scene scene;
    private Stage stage;
    private Group canvas;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private SpriteBatch batch;
    private Imagen fondo;
    private TopBar topBar;
    private History history;

    private Map<Integer, Actor> decisionNodeMap = new HashMap<>();
    private List<int[]> decisionLinks = new ArrayList<>();

    public AdminVisualStoryEditorScreen(String username, Scene scene,History history) {
        this.username = username;
        this.scene = scene;
        this.history=history;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDOBLOC);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        font = UIStyle.getFont();
        topBar = new TopBar(username, stage, font);
        shapeRenderer = new ShapeRenderer();

        canvas = new Group();
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setFillParent(true);
        stage.addActor(scrollPane);

        List<Decision> decisions = DecisionManager.getDecisionsBySceneId(scene.getId());
        float offsetX = 100;
        float offsetY = 400;
        for (int i = 0; i < decisions.size(); i++) {
            Decision decision = decisions.get(i);
            createDecisionNode(decision, offsetX + i * 250, offsetY - i * 100);
        }

        for (Decision decision : decisions) {
            List<Option> options = OptionManager.getOptionsByDecisionId(decision.getId());
            for (Option option : options) {
                Integer nextId = option.getNextDecisionId();
                if (nextId != null && nextId > 0) {
                    decisionLinks.add(new int[]{decision.getId(), nextId});
                }
            }
        }

        TextButton addDecisionBtn = new TextButton("+ Añadir Decisión", UIStyle.getButtonStyle());
        addDecisionBtn.setPosition(30, 30);
        addDecisionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showNewDecisionDialog();
            }
        });
        stage.addActor(addDecisionBtn);

        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        ImageButton backArrowButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backArrowButton.setSize(100, 100);
        backArrowButton.setPosition(10, Gdx.graphics.getHeight() - 150);
        backArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new AdminEditChapterScreen(username, ChapterManager.getChapterById(scene.getChapterId()),history));
            }
        });
        stage.addActor(backArrowButton);
    }

    private void createDecisionNode(Decision decision, float x, float y) {
        Table node = new Table();
        node.setSize(300, 150);
        node.setBackground(new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(300, 150, new Color(0.3f, 0.3f, 0.3f, 0.7f), 0.7f))));
        node.setPosition(x, y);

        Label titleLabel = new Label("Decisión " + decision.getId(), UIStyle.getLabelStyle());
        node.add(titleLabel).expand().center().row();

        node.addListener(new DragListener() {
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                node.moveBy(x - node.getWidth() / 2, y - node.getHeight() / 2);
            }
        });

        node.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showEditDecisionDialog(decision);
            }
        });

        canvas.addActor(node);
        decisionNodeMap.put(decision.getId(), node);
    }

    private void showNewDecisionDialog() {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(
            UIStyle.createRoundedPixmap(500, 400, new Color(0.3f, 0.2f, 0.5f, 1f), 1f)));

        Dialog dialog = new Dialog("Nueva Decisión", dialogStyle);

        TextField orderField = new TextField("", UIStyle.getTextFieldStyle());
        TextField dialogueField = new TextField("", UIStyle.getTextFieldStyle());
        SelectBox<String> characterBox = new SelectBox<>(UIStyle.getSelectBoxStyle());

        List<CharacterGame> characters = CharacterManager.getAllCharacters();
        Map<String, Integer> characterMap = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (CharacterGame c : characters) {
            characterMap.put(c.getName(), c.getId());
            names.add(c.getName());
        }
        characterBox.setItems(names.toArray(new String[0]));

        Label feedback = new Label("", UIStyle.getLabelStyle());
        feedback.setWrap(true);
        feedback.setColor(Color.RED); // asegurarse que se vea como error

        dialog.getContentTable().add(new Label("Orden:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(orderField).width(300).pad(5).row();

        dialog.getContentTable().add(new Label("Diálogo:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(dialogueField).width(300).pad(5).row();

        dialog.getContentTable().add(new Label("Personaje:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(characterBox).width(300).pad(5).row();

        TextButton saveBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String orderText = orderField.getText().trim();

                if (orderText.isEmpty()) {
                    UIStyle.showError(feedback, "El campo de orden no puede estar vacío.");
                    return;
                }
                if (!orderText.matches("\\d+")) {
                    UIStyle.showError(feedback, "El orden debe ser un número válido.");
                    return;
                }

                try {
                    int order = Integer.parseInt(orderText);
                    String dialogue = dialogueField.getText().trim();
                    int charId = characterMap.get(characterBox.getSelected());

                    Decision decision = new Decision();
                    decision.setSceneId(scene.getId());
                    decision.setDecisionOrder(order);
                    decision.setCharacterId(charId);
                    decision.setDialogue(dialogue);

                    int id = DecisionManager.createDecision(decision);
                    if (id > 0) {
                        decision.setId(id);
                        createDecisionNode(decision, 200 + canvas.getChildren().size * 250, 300);
                        dialog.hide();
                        dialog.remove();
                    } else {
                        UIStyle.showError(feedback, "Error al crear la decisión.");
                    }
                } catch (Exception e) {
                    UIStyle.showError(feedback, "Error inesperado al crear la decisión.");
                }
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(saveBtn).pad(10);
        dialog.getButtonTable().add(cancelBtn).pad(10);
        dialog.getContentTable().add(feedback).colspan(2).padTop(10).width(400).row();

        dialog.show(stage);
    }


    private void showEditDecisionDialog(Decision decision) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(0.3f, 0.2f, 0.5f, 1f), 1f)));

        Dialog dialog = new Dialog("Editar Decisión", dialogStyle);

        TextField dialogueField = new TextField(decision.getDialogue(), UIStyle.getTextFieldStyle());
        List<Option> options = OptionManager.getOptionsByDecisionId(decision.getId());

        Label feedback = new Label("", UIStyle.getLabelStyle());
        feedback.setWrap(true);

        dialog.getContentTable().add(new Label("Diálogo:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(dialogueField).width(400).pad(5).row();

        dialog.getContentTable().add(new Label("Opciones:", UIStyle.getLabelStyle())).colspan(2).padTop(10).row();

        for (Option option : options) {
            Table optionRow = new Table();
            String optionText = String.format("%s → %s", "->" + option.getText(),
                option.getNextDecisionId() != null ? "// Next Decisión " + option.getNextDecisionId() : "FIN");
            Label optionLabel = new Label(optionText, UIStyle.getLabelStyle());
            optionLabel.setEllipsis(true);
            optionLabel.setWidth(250);
            optionLabel.setWrap(false);
            optionRow.add(optionLabel).width(250).left().pad(5);

            TextButton editBtn = new TextButton("Editar", UIStyle.getButtonStyle());
            editBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showEditOptionDialog(option, decision);
                    dialog.hide();
                }
            });
            optionRow.add(editBtn).width(100).padLeft(10);

            TextButton deleteBtn = new TextButton("Eliminar", UIStyle.getButtonStyle());
            deleteBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    OptionManager.deleteOption(option.getId());
                    dialog.hide();
                    showEditDecisionDialog(DecisionManager.getDecisionById(decision.getId())); // Refresca diálogo
                }
            });
            optionRow.add(deleteBtn).width(100).padLeft(10);

            dialog.getContentTable().add(optionRow).colspan(2).pad(5).row();
        }

        TextButton addOptionBtn = new TextButton("+ Añadir Opción", UIStyle.getButtonStyle());
        addOptionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showAddOptionDialog(decision);
                dialog.hide();
            }
        });
        dialog.getContentTable().add(addOptionBtn).colspan(2).padTop(10).row();

        TextButton saveBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                decision.setDialogue(dialogueField.getText().trim());
                DecisionManager.updateDecision(decision);
                dialog.hide();
            }
        });

        TextButton deleteBtn = new TextButton("Eliminar", UIStyle.getButtonStyle());
        deleteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                showDeleteDecisionDialog(decision);
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(saveBtn).pad(10);
        dialog.getButtonTable().add(deleteBtn).pad(10);
        dialog.getButtonTable().add(cancelBtn).pad(10);
        dialog.getContentTable().add(feedback).colspan(2).padTop(10).row();

        dialog.show(stage);
    }

    private void showDeleteDecisionDialog(Decision decision) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(0.6f, 0.2f, 0.2f, 1f), 1f))
        );

        Dialog confirmDialog = new Dialog("Eliminar Decisión", dialogStyle);
        Label warning = new Label("¿Estás seguro de que deseas eliminar esta decisión?\nEsto eliminará también sus opciones asociadas.", UIStyle.getLabelStyle());
        warning.setWrap(true);
        warning.setAlignment(Align.center);

        confirmDialog.getContentTable().add(warning).width(400).pad(20).row();

        TextButton confirmBtn = new TextButton("Sí, eliminar", UIStyle.getButtonStyle());
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                DecisionManager.deleteDecision(decision.getId());
                confirmDialog.hide();
                Render.app.setScreen(new AdminVisualStoryEditorScreen(username, scene, history)); // Recarga pantalla
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


    private void showAddOptionDialog(Decision decision) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(0.3f, 0.2f, 0.5f, 1f), 1f))
        );

        Dialog dialog = new Dialog("Añadir Opción", dialogStyle);

        TextField textField = new TextField("(Continuar)", UIStyle.getTextFieldStyle());
        TextField pointsMField = new TextField("0", UIStyle.getTextFieldStyle());
        TextField pointsRField = new TextField("0", UIStyle.getTextFieldStyle());
        SelectBox<String> nextDecisionBox = new SelectBox<>(UIStyle.getSelectBoxStyle());

        // Estado psicológico como SelectBox
        SelectBox<String> stateBox = new SelectBox<>(UIStyle.getSelectBoxStyle());
        stateBox.setItems("NULL", "STABLE", "SLIGHTLY_ANXIOUS", "ANXIOUS", "DISTRESSED");

        Map<String, Integer> decisionMap = new HashMap<>();
        List<String> items = new ArrayList<>();
        for (Decision d : DecisionManager.getDecisionsBySceneId(scene.getId())) {
            if (d.getId() != decision.getId()) {
                String label = "" + d.getId();
                decisionMap.put(label, d.getId());
                items.add(label);
            }
        }
        items.add("Ninguna");
        nextDecisionBox.setItems(items.toArray(new String[0]));

        Label feedback = new Label("", UIStyle.getLabelStyle());

        dialog.getContentTable().add(new Label("Texto:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(textField).width(300).pad(5).row();
        dialog.getContentTable().add(new Label("Puntos M:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(pointsMField).width(300).pad(5).row();
        dialog.getContentTable().add(new Label("Puntos R:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(pointsRField).width(300).pad(5).row();
        dialog.getContentTable().add(new Label("Siguiente Decisión:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(nextDecisionBox).width(300).pad(5).row();
        dialog.getContentTable().add(new Label("Estado Psicológico Requerido:", UIStyle.getLabelStyle())).left().pad(5);
        dialog.getContentTable().add(stateBox).width(300).pad(5).row();

        TextButton saveBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    Option option = new Option();
                    option.setDecisionId(decision.getId());
                    option.setText(textField.getText().trim());
                    option.setPointsM(Integer.parseInt(pointsMField.getText().trim()));
                    option.setPointsR(Integer.parseInt(pointsRField.getText().trim()));

                    String selectedState = stateBox.getSelected();
                    option.setRequiredPsychologicalState("Ninguno".equals(selectedState) ? "" : selectedState);

                    String selected = nextDecisionBox.getSelected();
                    if (!"Ninguna".equals(selected)) {
                        option.setNextDecisionId(decisionMap.get(selected));
                    }

                    OptionManager.createOption(option);
                    dialog.hide();
                } catch (Exception e) {
                    feedback.setText("Error al guardar la opción.");
                    feedback.setColor(Color.RED);
                }
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(saveBtn).pad(10);
        dialog.getButtonTable().add(cancelBtn).pad(10);
        dialog.getContentTable().add(feedback).colspan(2).padTop(10).row();

        dialog.show(stage);
    }


    private void showEditOptionDialog(Option option, Decision currentDecision) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 400, new Color(0.3f, 0.2f, 0.5f, 1f), 1f))
        );

        Dialog dialog = new Dialog("Editar Opción", dialogStyle);

        // TextArea con scroll
        TextArea textArea = new TextArea(option.getText(), UIStyle.getTextFieldStyle());
        textArea.setPrefRows(5);
        ScrollPane textScroll = new ScrollPane(textArea);
        textScroll.setFadeScrollBars(false);
        textScroll.setScrollingDisabled(true, false);

        TextField pointsMField = new TextField(String.valueOf(option.getPointsM()), UIStyle.getTextFieldStyle());
        TextField pointsRField = new TextField(String.valueOf(option.getPointsR()), UIStyle.getTextFieldStyle());
        SelectBox<String> nextDecisionBox = new SelectBox<>(UIStyle.getSelectBoxStyle());

        // SelectBox para el estado psicológico requerido
        SelectBox<String> stateBox = new SelectBox<>(UIStyle.getSelectBoxStyle());
        stateBox.setItems("NULL", "STABLE", "SLIGHTLY_ANXIOUS", "ANXIOUS", "DISTRESSED");
        if (option.getRequiredPsychologicalState() == null || option.getRequiredPsychologicalState().isEmpty()) {
            stateBox.setSelected("Ninguno");
        } else {
            stateBox.setSelected(option.getRequiredPsychologicalState());
        }

        Map<String, Integer> decisionMap = new HashMap<>();
        List<String> items = new ArrayList<>();
        for (Decision d : DecisionManager.getDecisionsBySceneId(scene.getId())) {
            if (d.getId() != currentDecision.getId()) {
                String label = String.valueOf(d.getId());
                decisionMap.put(label, d.getId());
                items.add(label);
            }
        }
        items.add("Ninguna");
        nextDecisionBox.setItems(items.toArray(new String[0]));
        if (option.getNextDecisionId() != null) {
            nextDecisionBox.setSelected(String.valueOf(option.getNextDecisionId()));
        } else {
            nextDecisionBox.setSelected("Ninguna");
        }

        Label feedback = new Label("", UIStyle.getLabelStyle());

        Table contentTable = new Table();
        contentTable.add(new Label("Texto:", UIStyle.getLabelStyle())).left().pad(5);
        contentTable.add(textScroll).width(300).height(100).pad(5).row();
        contentTable.add(new Label("Puntos M:", UIStyle.getLabelStyle())).left().pad(5);
        contentTable.add(pointsMField).width(300).pad(5).row();
        contentTable.add(new Label("Puntos R:", UIStyle.getLabelStyle())).left().pad(5);
        contentTable.add(pointsRField).width(300).pad(5).row();
        contentTable.add(new Label("Siguiente Decisión:", UIStyle.getLabelStyle())).left().pad(5);
        contentTable.add(nextDecisionBox).width(300).pad(5).row();
        contentTable.add(new Label("Estado Psicológico Requerido:", UIStyle.getLabelStyle())).left().pad(5);
        contentTable.add(stateBox).width(300).pad(5).row();

        dialog.getContentTable().add(contentTable).colspan(2).pad(10).row();

        TextButton saveBtn = new TextButton("Guardar", UIStyle.getButtonStyle());
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    option.setText(textArea.getText().trim());
                    option.setPointsM(Integer.parseInt(pointsMField.getText().trim()));
                    option.setPointsR(Integer.parseInt(pointsRField.getText().trim()));

                    String selectedState = stateBox.getSelected();
                    option.setRequiredPsychologicalState("Ninguno".equals(selectedState) ? "" : selectedState);

                    String selected = nextDecisionBox.getSelected();
                    if (!"Ninguna".equals(selected)) {
                        option.setNextDecisionId(decisionMap.get(selected));
                    } else {
                        option.setNextDecisionId(null);
                    }

                    OptionManager.updateOption(option);
                    dialog.hide();
                } catch (Exception e) {
                    feedback.setText("Error al guardar la opción.");
                    feedback.setColor(Color.RED);
                }
            }
        });

        TextButton cancelBtn = new TextButton("Cancelar", UIStyle.getButtonStyle());
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getContentTable().add(feedback).colspan(2).padTop(15).row();
        dialog.getButtonTable().add(saveBtn).pad(10);
        dialog.getButtonTable().add(cancelBtn).pad(10);

        dialog.show(stage);
    }





    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        fondo.draw();
        batch.end();

        stage.act(delta);
        stage.draw();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        for (int[] link : decisionLinks) {
            Actor from = decisionNodeMap.get(link[0]);
            Actor to = decisionNodeMap.get(link[1]);
            if (from != null && to != null) {
                float startX = from.getX() + from.getWidth() / 2;
                float startY = from.getY() + from.getHeight() / 2;
                float endX = to.getX() + to.getWidth() / 2;
                float endY = to.getY() + to.getHeight() / 2;
                shapeRenderer.line(startX, startY, endX, endY);
            }
        }
        shapeRenderer.end();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true);
        topBar.updateSize();}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); shapeRenderer.dispose(); }
}
