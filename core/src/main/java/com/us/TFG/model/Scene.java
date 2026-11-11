package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

import java.util.List;

@Table(name = "scenes")
public class Scene {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;

    @Column(name = "chapter_id", type = "INTEGER", notNull = true) // Relación con capítulos
    private int chapterId;

    @Column(name = "background_image", type = "TEXT") // Imagen de fondo de la escena
    private String backgroundImage;

    @Column(name = "scene_order", type = "INTEGER") // Orden de la escena dentro del capítulo
    private int sceneOrder;


    // Constructor principal
    public Scene(int id, int chapterId, String backgroundImage, int sceneOrder) {
        this.id = id;
        this.chapterId = chapterId;
        this.backgroundImage = backgroundImage;
        this.sceneOrder = sceneOrder;
    }

    // Constructor vacío
    public Scene() {}

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public int getSceneOrder() {
        return sceneOrder;
    }

    public void setSceneOrder(int sceneOrder) {
        this.sceneOrder = sceneOrder;
    }



    @Override
    public String toString() {
        return "Scene{" +
            "id=" + id +
            ", chapterId=" + chapterId +
            ", backgroundImage='" + backgroundImage + '\'' +
            ", sceneOrder=" + sceneOrder +
            '}';
    }
}
