package com.us.TFG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.us.TFG.MyTfgGame;

public class Render {
    public static SpriteBatch batch;


    public static MyTfgGame app;

    public static void cleanScreen(){
        Gdx.gl.glClearColor(1,1,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
