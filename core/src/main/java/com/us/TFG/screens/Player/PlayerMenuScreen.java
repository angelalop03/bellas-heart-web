package com.us.TFG.screens.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.screens.Player.Game.ChargeGameScreen;
import com.us.TFG.screens.Player.Game.CreateGameScreen;
import com.us.TFG.screens.Player.Game.SceneGameScreen;
import com.us.TFG.screens.Player.TutorialGameScreen;
import com.us.TFG.utils.*;

public class PlayerMenuScreen implements Screen {

    private Stage stage;
    private SpriteBatch b;
    private Skin skin;
    private Imagen fondo;
    private TopBar topBar;
    private String username;
    private ImageButton creditsButton;
    private ImageButton mobilButton;

    public PlayerMenuScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.MENU);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        b = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = UIStyle.getSkin();
        topBar = new TopBar(username, stage, UIStyle.getFont());

        // Crear botones
        TextButton newGameButton = new TextButton("Nueva Partida", UIStyle.getButtonStyle());
        TextButton loadGameButton = new TextButton("Cargar Partida", UIStyle.getButtonStyle());
        TextButton historialButton = new TextButton("Historial", UIStyle.getButtonStyle());
        TextButton newsForumButton = new TextButton("Foro de Noticias", UIStyle.getButtonStyle());
        TextButton instructionsButton = new TextButton("Tutorial", UIStyle.getButtonStyle());

        float buttonWidth = 300;
        float buttonHeight = 75;
        float spacing = 20;

        // Añadir listeners
        newGameButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new CreateGameScreen(username));
            }
        });
        loadGameButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ChargeGameScreen(username));
            }
        });
        historialButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new PlayerHistorialScreen(username));
            }
        });
        newsForumButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ForumScreen(username));
            }
        });
        instructionsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new TutorialGameScreen(username));
            }
        });

        // Tabla para alinear botones
        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.center().padTop(200);

        buttonTable.add(newGameButton).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();
        buttonTable.add(loadGameButton).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();
        buttonTable.add(historialButton).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();
        buttonTable.add(newsForumButton).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();
        buttonTable.add(instructionsButton).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();

        stage.addActor(buttonTable);

        // Botón de créditos con imagen de interrogación
        creditsButton = new ImageButton(new TextureRegionDrawable(new Texture(Gdx.files.internal("photos/question.png"))));
        creditsButton.setSize(40, 40);
        creditsButton.setPosition(Gdx.graphics.getWidth() - 70, 20);
        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new CreditsScreen(username));
            }
        });
        stage.addActor(creditsButton);

        // Resources
        mobilButton = new ImageButton(new TextureRegionDrawable(new Texture(Gdx.files.internal("photos/Movil.png"))));
        mobilButton.setSize(150, 150);
        mobilButton.setPosition(20, 20);
        mobilButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new ResourcesScreen(username));
            }
        });
        stage.addActor(mobilButton);
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
        creditsButton.setPosition(Gdx.graphics.getWidth() - 70, 50);
        mobilButton.setPosition(20, 20);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
