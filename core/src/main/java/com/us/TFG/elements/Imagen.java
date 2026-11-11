package com.us.TFG.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.us.TFG.utils.Render;

public class Imagen {
    private Texture t;
    private Sprite s;

    public Imagen(String rute){
        t= new Texture(rute);
        s= new Sprite(t);

    }

    public void draw(){
        s.draw(Render.batch);
    }

    public void setTransparency(float transparency){
        s.setAlpha(transparency);

    }

    public void setSize( float width, float height){
        s.setSize(width, height);
    }
}
