package com.us.TFG.screens.General;

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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.us.TFG.elements.Imagen;
import com.us.TFG.utils.*;

public class OpenScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Skin skin;
    private BitmapFont font;
    private Imagen fondo;


    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = UIStyle.getSkin();
        batch = Render.batch;
        fondo = new Imagen(Resources.MENU);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


        // Botón de inicio de sesión
        TextButton loginButton = new TextButton("Iniciar Sesión", UIStyle.getButtonStyle());
        loginButton.setSize(300, 75);
        loginButton.setPosition(Gdx.graphics.getWidth() / 2f - 150, 200);
        stage.addActor(loginButton);

        // Agregar el listener para cambiar a LoginScreen
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new LoginScreen());

            }
        });

        // Botón de registro
        TextButton signUpButton = new TextButton("Registrarse", UIStyle.getButtonStyle());
        signUpButton.setSize(300, 75);
        signUpButton.setPosition(Gdx.graphics.getWidth() / 2f - 150, 100);
        stage.addActor(signUpButton);

        // Agregar el listener para cambiar a LoginScreen
        signUpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new RegisterScreen());
            }
        });

    }

    @Override
    public void render(float delta) {
        batch.begin();
            fondo.draw();
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        //musicManager.dispose();
    }

}
