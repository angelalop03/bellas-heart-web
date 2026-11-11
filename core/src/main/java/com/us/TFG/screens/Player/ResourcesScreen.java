package com.us.TFG.screens.Player;

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
import com.us.TFG.utils.*;

public class ResourcesScreen implements Screen {

    private Stage stage;
    private SpriteBatch batch;
    private Imagen fondo;
    private BitmapFont font;
    private TopBar topBar;
    private String username;
    private ImageButton backButton;

    public ResourcesScreen(String username) {
        this.username = username;
    }

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDON);
        fondo.setSize(Config.WIDTH, Config.HEIGHT);
        batch = Render.batch;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        font = UIStyle.getFont();
        topBar = new TopBar(username, stage, font);

        // Botón de retroceso
        Texture backArrowTexture = new Texture(Gdx.files.internal("photos/flecha.png"));
        backButton = new ImageButton(new TextureRegionDrawable(backArrowTexture));
        backButton.setSize(80, 80);
        backButton.setPosition(10, Gdx.graphics.getHeight() - 150);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Render.app.setScreen(new PlayerMenuScreen(username));
            }
        });
        stage.addActor(backButton);

        // Recuadro morado translúcido
        Pixmap pixmap = new Pixmap(1000, 600, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.6f, 0.5f)); // morado translúcido
        pixmap.fill();
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        Table backgroundBox = new Table();
        backgroundBox.setBackground(backgroundDrawable);
        backgroundBox.setSize(1000, 600);
        backgroundBox.setPosition((Gdx.graphics.getWidth() - 1000) / 2f, 100);

        // Contenido principal dentro del recuadro
        Table content = new Table();
        content.center().top().padTop(30);

        Label title = new Label("Recursos de ayuda contra la violencia machista", UIStyle.getTitleLabelStyle());
        title.setAlignment(Align.center);
        title.setWrap(true);
        content.add(title).width(800).padBottom(30).row();

        Label info = new Label("Si estás sufriendo violencia de género, puedes contactar con ayuda especializada. El 016 es gratuito y no deja rastro en la factura telefónica.", UIStyle.getLabelStyle());
        info.setWrap(true);
        info.setAlignment(Align.center);
        content.add(info).width(700).padBottom(20).row();

        Label hotline = new Label("📞 Llama al 016", new Label.LabelStyle(font, Color.PINK));
        hotline.setFontScale(1.5f);
        hotline.setAlignment(Align.center);
        content.add(hotline).padBottom(30).row();

        // Botones con enlaces externos
        TextButton link1 = new TextButton("Web del 016", UIStyle.getButtonStyle());
        link1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://violenciagenero.igualdad.gob.es/informacion-3/recursos/telefono016/");
            }
        });

        TextButton link2 = new TextButton("Dónde acudir en caso de violencia de género", UIStyle.getButtonStyle());
        link2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://www.juntadeandalucia.es/temas/familias-igualdad/mujeres/violencia-genero/paginas/donde-acudir.html");
            }
        });

        TextButton link3 = new TextButton("Fundación Ana Bella", UIStyle.getButtonStyle());
        link3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://www.fundacionanabella.org/");
            }
        });


        content.add(link1).width(550).height(60).padBottom(15).row();
        content.add(link2).width(550).height(60).padBottom(15).row();
        content.add(link3).width(550).height(60).padBottom(15).row();

        backgroundBox.add(content).expand().fill();
        stage.addActor(backgroundBox);
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
        topBar.updateSize();
        if (backButton != null) {
            backButton.setPosition(10, height - 150);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
