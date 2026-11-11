package com.us.TFG.screens.General;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.us.TFG.elements.Imagen;
import com.us.TFG.utils.Config;
import com.us.TFG.utils.Render;
import com.us.TFG.utils.Resources;

public class LogoScreen implements Screen {
    Imagen fondo;
    SpriteBatch b;
    float a =0;
    float contTime=0, timeWait = 5;
    boolean fadeFinish =false, finish = false;
    float contTimeFinish=0,timeFinish=5;

    @Override
    public void show() {
        fondo = new Imagen(Resources.FONDO);
        b= Render.batch;
        fondo.setSize(Config.WIDTH, Config.HEIGHT);

        fondo.setTransparency(1);
    }

    @Override
    public void render(float delta) {
        Render.cleanScreen();

        processTransparency();

        b.begin();
        fondo.draw();
        b.end();


    }

    private void processTransparency() {
        if(!fadeFinish) {
            a+=0.01f;
            if(a>1) {
                a=1;
                fadeFinish =true;
            }
        } else {
            contTime +=0.05f;
            if(contTime> timeWait){
                a-=0.01f;
                if(a<0) {
                    a=0;
                    finish =true;
                }
            }
        }
        fondo.setTransparency(a);

        if(finish){
            contTimeFinish +=0.1f;
            if(contTimeFinish> timeFinish){
               Render.app.setScreen(new OpenScreen());
                }
        }

    }

    @Override
    public void resize(int width, int height) {
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
    }
}
