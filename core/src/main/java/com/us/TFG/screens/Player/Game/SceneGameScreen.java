package com.us.TFG.screens.Player.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.manager.*;
import com.us.TFG.model.*;
import com.us.TFG.screens.Player.PlayerMenuScreen;
import com.us.TFG.utils.Render;
import com.us.TFG.utils.UIStyle;

import java.util.List;


public class SceneGameScreen implements Screen {

    private Stage stage;
    private AssetManager assetManager;
    private Scene currentScene;
    private Decision currentDecision;
    private CharacterGame currentCharacter;
    private int chapterId;
    private int gameId;
    private String username;
    private BitmapFont font;
    private Image background;
    private Image characterImage;
    private Label dialogueLabel;
    private Table optionsTable;
    private Skin skin;
    private Texture bgTexture, characterTexture;
    private ProgressBar psychologicalBar, relationshipBar;
    private int previousPsychologicalPoints =-1;
    private int previousRelationshipPoints=-1;
    private Image dialogueBackground;
    private Music backgroundMusic;
    private ImageButton settingsButton;


    public SceneGameScreen(int gameId, int chapterId, String username) {
        this.gameId = gameId;
        this.chapterId = chapterId;
        this.username = username;
        this.assetManager = new AssetManager();
        setupBars();

    }
    private void loadGameData(int gameId) {
        Game game = GameManager.getGameById(gameId);
        if (game != null) {
            this.currentScene = SceneManager.getSceneById(game.getCurrentSceneId());
            this.currentDecision = DecisionManager.getDecisionById(game.getCurrentDecisionId());
            if (currentDecision != null) {
                this.currentCharacter = CharacterManager.getCharacterById(currentDecision.getCharacterId());
                loadTexturesForCurrentScene();
                assetManager.finishLoading();
            }
        }
    }

    private void loadTexturesForCurrentScene() {
        if (currentScene != null) {
            String backgroundPath = "photos/Scenes/" + currentScene.getBackgroundImage();

            if (!assetManager.isLoaded(backgroundPath)) {
                if (Gdx.files.internal(backgroundPath).exists()) {
                    assetManager.load(backgroundPath, Texture.class);
                    assetManager.finishLoading(); // Asegúrate de que se cargue antes de obtenerla
                    System.out.println("Textura cargada correctamente: " + backgroundPath);
                } else {
                    System.err.println("Error: La textura de fondo no existe en la ruta especificada: " + backgroundPath);
                }
            } else {
                System.out.println("Textura ya cargada: " + backgroundPath);
            }

            if (currentCharacter != null) {
                String characterPath = "photos/Characters/" + currentCharacter.getImagePath();

                if (!assetManager.isLoaded(characterPath)) {
                    if (Gdx.files.internal(characterPath).exists()) {
                        assetManager.load(characterPath, Texture.class);
                        assetManager.finishLoading();
                        System.out.println("Textura de personaje cargada correctamente: " + characterPath);
                    } else {
                        System.err.println("Error: La textura de personaje no existe en la ruta especificada: " + characterPath);
                    }
                } else {
                    System.out.println("Textura de personaje ya cargada: " + characterPath);
                }
            }
        }
    }


    private void setupBars() {
        stage = new Stage(new ScreenViewport());

        int maxPoints = 100;
        int psychologicalPoints = GameManager.getGameById(gameId).getPointsM();
        int relationshipPoints = GameManager.getGameById(gameId).getPointsR();

        psychologicalBar = createBar(psychologicalPoints, maxPoints);
        relationshipBar = createBar(relationshipPoints, maxPoints);

        psychologicalBar.setPosition(Gdx.graphics.getWidth() - 70, Gdx.graphics.getHeight() - 400);
        relationshipBar.setPosition(Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 400);

        stage.addActor(psychologicalBar);
        stage.addActor(relationshipBar);

        Gdx.input.setInputProcessor(stage);  // Necesario para que el Stage reciba eventos de input
    }

    private ProgressBar createBar(int value, int max) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        Pixmap pixmap = new Pixmap(20, 200, Pixmap.Format.RGBA8888);

        // Fondo de la barra (gris oscuro)
        pixmap.setColor(Color.LIGHT_GRAY);
        pixmap.fill();
        style.background = new TextureRegionDrawable(new Texture(pixmap));

        pixmap.dispose();
        pixmap = new Pixmap(20, 200, Pixmap.Format.RGBA8888);

        // Parte de progreso de la barra (rosado)
        Color pink = new Color(1f, 0.4f, 0.7f, 1f);
        pixmap.setColor(pink);
        pixmap.fill();
        style.knobBefore = new TextureRegionDrawable(new Texture(pixmap)); // La parte llenada

        pixmap.dispose();

        ProgressBar bar = new ProgressBar(0, max, 1, true, style); // Orientación vertical
        bar.setValue(value);
        bar.setSize(20, 200); // Establece el tamaño real de la barra
        bar.setAnimateDuration(0.5f); // Permitir animación suave

        return bar;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        if (backgroundMusic == null) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/musica-lofi.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
            backgroundMusic.play();
        }
        Texture settingsTexture = new Texture(Gdx.files.internal("photos/Settings.png")); // <- usa tu imagen real
        settingsButton = new ImageButton(new TextureRegionDrawable(settingsTexture));
        settingsButton.setSize(50, 50);
        settingsButton.setPosition(Gdx.graphics.getWidth() - 60, Gdx.graphics.getHeight() - 60); // Arriba a la derecha
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showVolumeSettingsDialog();
            }
        });
        // Check if the chapter has already been played
        if (ChapterManager.isChapterPlayed(gameId, chapterId)) {
            stage = new Stage(new ScreenViewport());
            Gdx.input.setInputProcessor(stage);
            displayErrorMessage("Ya has jugado este capítulo.");
        } else {
            loadGameData(gameId);
            if (currentDecision == null) {
                displayErrorMessage("No se puede jugar este capítulo");
                return;
            }

            if (currentScene != null) {
                String backgroundPath = "photos/Scenes/" + currentScene.getBackgroundImage();
                if (Gdx.files.internal(backgroundPath).exists()) {
                    bgTexture = new Texture(Gdx.files.internal(backgroundPath));
                } else {
                    Pixmap pixmap = new Pixmap(1280, 720, Pixmap.Format.RGBA8888);
                    pixmap.setColor(Color.DARK_GRAY);
                    pixmap.fill();
                    bgTexture = new Texture(pixmap);
                    pixmap.dispose();
                }
                background = new Image(new TextureRegionDrawable(bgTexture));
                background.setFillParent(true);
                stage.addActor(background);
            } else {
                displayErrorMessage("Error al cargar la textura de fondo.");
            }

            if (currentCharacter != null && assetManager.isLoaded("photos/Characters/" + currentCharacter.getImagePath())) {
                characterTexture = assetManager.get("photos/Characters/" + currentCharacter.getImagePath(), Texture.class);
                characterImage = new Image(new TextureRegionDrawable(characterTexture));
                characterImage.setSize(390, 600);
                characterImage.setPosition(180, 0);
                stage.addActor(characterImage);
            }

            // Crear el fondo blanco con bordes redondeados para los diálogos
            int cornerRadius = 20;  // Radio de las esquinas redondeadas
            int backgroundWidth = 500;
            int backgroundHeight = 300;

            Pixmap pixmap = new Pixmap(backgroundWidth, backgroundHeight, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.SourceOver);
            pixmap.setColor(Color.CLEAR);
            pixmap.fill();

            pixmap.setColor(Color.WHITE);

            pixmap.fillRectangle(cornerRadius, 0, backgroundWidth - 2 * cornerRadius, backgroundHeight);
            pixmap.fillRectangle(0, cornerRadius, backgroundWidth, backgroundHeight - 2 * cornerRadius);
            pixmap.fillCircle(cornerRadius, cornerRadius, cornerRadius);
            pixmap.fillCircle(backgroundWidth - cornerRadius - 1, cornerRadius, cornerRadius);
            pixmap.fillCircle(cornerRadius, backgroundHeight - cornerRadius - 1, cornerRadius);
            pixmap.fillCircle(backgroundWidth - cornerRadius - 1, backgroundHeight - cornerRadius - 1, cornerRadius);

            Texture backgroundTexture = new Texture(pixmap);
            pixmap.dispose();

            dialogueBackground = new Image(new TextureRegionDrawable(backgroundTexture));
            stage.addActor(dialogueBackground);

            dialogueLabel = new Label(currentDecision.getDialogue(), new Label.LabelStyle(UIStyle.getFont(), Color.GRAY));
            dialogueLabel.setWrap(true);
            dialogueLabel.setAlignment(Align.topLeft);
            dialogueLabel.setWidth(backgroundWidth - 40);  // Restar padding para que el texto no quede pegado a los bordes
            dialogueLabel.setPosition(Gdx.graphics.getWidth() - 650, Gdx.graphics.getHeight() - 250); // Cambia la posición según necesites

            dialogueBackground.setPosition(dialogueLabel.getX() - 20, dialogueLabel.getY() - 20);
            dialogueBackground.setSize(backgroundWidth, backgroundHeight);

            stage.addActor(dialogueBackground);
            stage.addActor(dialogueLabel);



            optionsTable = new Table();
            optionsTable.setFillParent(true);
            optionsTable.bottom().pad(20);
            stage.addActor(optionsTable);

            showOptions();
            // Añadir las barras al escenario
            stage.addActor(psychologicalBar);
            stage.addActor(relationshipBar);
            stage.addActor(settingsButton);

        }
    }

    @Override
    public void render(float delta) {
        updateBars();

        Gdx.gl.glClearColor(0.36f, 0.27f, 0.49f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (dialogueLabel != null && dialogueBackground != null) {

            float padding = 20f; // Margen alrededor del texto
            float padding2 = 15f; // Margen alrededor del texto

            dialogueLabel.layout(); // Calcular el tamaño del texto
            float labelWidth = dialogueLabel.getWidth();
            float labelHeight = dialogueLabel.getPrefHeight();

            dialogueBackground.setSize(labelWidth+ padding*2 , labelHeight + padding * 2);
            dialogueBackground.setPosition(dialogueLabel.getX() - padding*2, dialogueLabel.getY()  - labelHeight+padding );
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        settingsButton.setPosition(width - 60, height - 60);

        if (characterImage != null) {
            characterImage.setSize(width * 0.3f, height * 0.775f);
            characterImage.setPosition(width * 0.14f, 0);
        }

        if (psychologicalBar != null && relationshipBar != null) {
            float barWidth = width * 0.02f;
            float barHeight = height * 0.3f;
            float spacing = barWidth + 10;

            psychologicalBar.setSize(barWidth, barHeight);
            relationshipBar.setSize(barWidth, barHeight);

            psychologicalBar.setPosition(width - spacing, height - barHeight - 250);
            relationshipBar.setPosition(width - spacing * 2, height - barHeight - 250);
        }

        if (dialogueLabel != null && dialogueBackground != null) {
            dialogueLabel.setPosition(width - 650, height - 250);
            float padding = 20f;
            dialogueLabel.layout();
            float labelWidth = dialogueLabel.getWidth();
            float labelHeight = dialogueLabel.getPrefHeight();
            dialogueBackground.setSize(labelWidth + padding * 2, labelHeight + padding * 2);
            dialogueBackground.setPosition(dialogueLabel.getX() - padding * 2, dialogueLabel.getY() - labelHeight + padding);
        }

        showOptions();
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }

    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }

    }
    private void updateBars() {
        int currentPsychologicalPoints = GameManager.getGameById(gameId).getPointsM();
        int currentRelationshipPoints = GameManager.getGameById(gameId).getPointsR();

        if (currentPsychologicalPoints != previousPsychologicalPoints) {
            psychologicalBar.setValue(currentPsychologicalPoints);
            previousPsychologicalPoints = currentPsychologicalPoints;
        }

        if (currentRelationshipPoints != previousRelationshipPoints) {
            relationshipBar.setValue(currentRelationshipPoints);
            previousRelationshipPoints = currentRelationshipPoints;
        }
    }
    private void displayErrorMessage(String message) {
        Label errorLabel = new Label(message, new Label.LabelStyle(UIStyle.getFont(), Color.RED));
        errorLabel.setAlignment(Align.center);
        errorLabel.setPosition(Gdx.graphics.getWidth() / 2f - 300, Gdx.graphics.getHeight() / 2f);
        errorLabel.setWidth(600);
        errorLabel.setWrap(true);
        stage.addActor(errorLabel);

        TextButton continueButton = new TextButton("Continuar", UIStyle.getButtonStyle());
        continueButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 100);
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ChapterCatalogScreen(username,gameId)); // Volver al menú principal
            }
        });
        stage.addActor(continueButton);
    }

    private void showVolumeSettingsDialog() {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = UIStyle.getFont();
        dialogStyle.titleFontColor = Color.WHITE;
        dialogStyle.background = new TextureRegionDrawable(new Texture(UIStyle.createRoundedPixmap(400, 200, new Color(0.5f, 0.0f, 0.5f, 1.0f), 0.8f)));

        Dialog volumeDialog = new Dialog("", dialogStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = UIStyle.getFont();
        labelStyle.fontColor = Color.WHITE;

        final Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, UIStyle.getSliderStyle());
        volumeSlider.setValue(backgroundMusic.getVolume());

        Label volumeLabel = new Label("Volumen: " + (int)(backgroundMusic.getVolume() * 100) + "%", labelStyle);
        volumeSlider.addListener(event -> {
            float value = volumeSlider.getValue();
            backgroundMusic.setVolume(value);
            volumeLabel.setText("Volumen: " + (int)(value * 100) + "%");
            return false;
        });

        TextButton closeButton = new TextButton("Cerrar", UIStyle.getButtonStyle());
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                volumeDialog.hide();
            }
        });

        volumeDialog.getContentTable().pad(20);
        volumeDialog.getContentTable().add(volumeLabel).padBottom(10).row();
        volumeDialog.getContentTable().add(volumeSlider).width(300).padBottom(20).row();
        volumeDialog.getContentTable().add(closeButton).width(150).center();

        volumeDialog.show(stage);
    }



    private void showOptions() {
        optionsTable.clear();

        //List<Option> availableOptions = OptionManager.getOptionsByDecisionId(currentDecision.getId());
        List<Option>  options = OptionManager.getAvailableOptions(currentDecision.getId(), GameManager.getGameById(gameId));
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = UIStyle.getFont();
        buttonStyle.fontColor = Color.WHITE;

        int optionWidth = 700;
        int optionHeight = 80;

        // Cantidad de opciones disponibles
        int numberOfOptions = options.size();

        // Ajustar la posición vertical en función del número de opciones
        int baseTopPadding = 200;   // Punto más bajo de inicio para opciones múltiples
        int maxTopPadding = 650;    // Punto más alto de inicio para una única opción

        // Calcular padding dinámico según la cantidad de opciones
        int topPadding = maxTopPadding - (numberOfOptions - 1) * 100; // Espaciado progresivo

        // Limitar el padding superior para que no se salga de la pantalla
        topPadding = Math.max(baseTopPadding, topPadding);

        // Configurar la tabla de opciones para que esté a la derecha de la pantalla
        optionsTable.setFillParent(true);
        optionsTable.bottom().right().padTop(topPadding).padRight(20);

        for (Option option : options) {

            // Crear un Pixmap con fondo rosado transparente sin bordes redondeados
            Pixmap pixmap = new Pixmap(optionWidth, optionHeight, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.SourceOver);

            // Color rosado con transparencia (ej: 80% de opacidad)
            pixmap.setColor(new Color(1f, 0.4f, 0.7f, 0.8f));
            pixmap.fillRectangle(0, 0, optionWidth, optionHeight);

            Texture optionTexture = new Texture(pixmap);
            pixmap.dispose();

            Image optionBackground = new Image(new TextureRegionDrawable(optionTexture));

            // Crear un contenedor para el fondo y el botón
            Table optionContainer = new Table();
            optionContainer.add(optionBackground).size(optionWidth, optionHeight);

            // Crear el botón de opción
            TextButton optionButton = new TextButton(option.getText(), buttonStyle);
            optionButton.getLabel().setWrap(true);
            optionButton.pad(10);

            optionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleOptionSelection(option);
                }
            });

            // Añadir el botón encima del fondo
            optionContainer.add(optionButton).padLeft(-optionWidth).width(optionWidth - 20).height(optionHeight - 20);

            // Añadir la opción a la tabla de opciones alineadas a la derecha
            optionsTable.add(optionContainer).pad(10).row();
        }
    }






    private void handleOptionSelection(Option option) {
        // Actualizar puntos en el juego
        GameManager.updatePoints(gameId, option.getPointsM(), option.getPointsR());
        //Actualice el PlayerSceneState
        PlayerDecisionStateManager.updateDecisionState(PlayerManager.getPlayerData(username).getId(),gameId, currentDecision.getId(),option.getId(), true);

        if (option.getNextDecisionId() != null) {
            // Cargar la siguiente decisión
            currentDecision = DecisionManager.getDecisionById(option.getNextDecisionId());
            DecisionManager.updateCurrentDecision(gameId, currentDecision.getId());


            currentCharacter = CharacterManager.getCharacterById(currentDecision.getCharacterId());
            updateCharacterImage();
            dialogueLabel.setText(currentDecision.getDialogue());
            showOptions();


        } else {
            // Comprobar si todas las decisiones de la escena están completas

            goToNextSceneOrChapter();
            System.out.println("GotoNextSceneOrChapter");

        }
    }

    private void updateBackgroundImage() {
        if (background != null) {
            stage.getActors().removeValue(background, true);
        }

        if (currentScene != null) {
            String backgroundPath = "photos/Scenes/" + currentScene.getBackgroundImage();
            if (Gdx.files.internal(backgroundPath).exists()) {
                bgTexture = new Texture(Gdx.files.internal(backgroundPath));
            } else {
                Pixmap pixmap = new Pixmap(1280, 720, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.DARK_GRAY);
                pixmap.fill();
                bgTexture = new Texture(pixmap);
                pixmap.dispose();
            }
            background = new Image(new TextureRegionDrawable(bgTexture));
            background.setFillParent(true);
            stage.addActor(background);
            background.toBack();

        }
    }

    private void goToNextSceneOrChapter() {
        Scene nextScene = SceneManager.getNextSceneInChapter(currentScene.getId(), chapterId);

        if (nextScene != null) {
            currentScene = nextScene;
            currentDecision = DecisionManager.getFirstDecisionBySceneId(currentScene.getId());
            updateBackgroundImage();

            if (currentDecision != null) {
                currentCharacter = CharacterManager.getCharacterById(currentDecision.getCharacterId());

                // Precargar las texturas necesarias antes de usarlas
                loadTexturesForCurrentScene();

                DecisionManager.updateCurrentDecision(gameId, currentDecision.getId());
                updateCharacterImage();
                dialogueLabel.setText(currentDecision.getDialogue());
            }
            SceneManager.updateCurrentScene(gameId, currentScene.getId());

            dialogueLabel.setText(currentDecision.getDialogue());
            showOptions();
        } else {
            System.out.println("No hay más escenas en este capítulo.");
            ChapterManager.markChapterAsPlayed(gameId, chapterId);

            int nextChapterId = ChapterManager.unlockNextChapter(gameId, chapterId);
            Render.app.setScreen(new ChapterCatalogScreen(username, gameId));

            if (nextChapterId > 0) {
                Scene firstScene = SceneManager.getFirstSceneOfChapter(nextChapterId);
                if (firstScene != null) {

                    currentScene = firstScene;
                    currentDecision = DecisionManager.getFirstDecisionBySceneId(firstScene.getId());
                    currentCharacter = (currentDecision != null) ? CharacterManager.getCharacterById(currentDecision.getCharacterId()) : null;
                    chapterId = nextChapterId;

                    loadTexturesForCurrentScene();

                    ChapterManager.updateCurrentChapter(gameId, nextChapterId);
                    SceneManager.updateCurrentScene(gameId, currentScene.getId());
                    DecisionManager.updateCurrentDecision(gameId, (currentDecision != null) ? currentDecision.getId() : 0);

                }
            } else {
                System.out.println("El next chapter id no se pilla bien");
            }
        }
    }


    private void updateCharacterImage() {
        if (characterImage != null) {
            stage.getActors().removeValue(characterImage, true);
        }

        if (currentCharacter != null) {
            String characterPath = "photos/Characters/" + currentCharacter.getImagePath();

            if (!assetManager.isLoaded(characterPath)) {
                assetManager.load(characterPath, Texture.class);
                assetManager.finishLoading();
            }

            characterTexture = assetManager.get(characterPath, Texture.class);

            characterImage = new Image(new TextureRegionDrawable(characterTexture));
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();

            characterImage.setSize(w * 0.3f, h * 0.775f);
            characterImage.setPosition(w * 0.14f, 0);


            stage.addActor(characterImage);
        }
    }

}
