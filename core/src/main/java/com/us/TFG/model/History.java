package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

@Table(name = "historys")
public class History {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;  // ID único para la base de datos

    @Column(name = "title", type = "TEXT", notNull = true)
    private String title;  // Titulo de la historia

    @Column(name = "published", type = "BOOLEAN", notNull = true, defaultValue = "false")
    private boolean published; // Indica si la historia está públicada

    @Column(name = "image_path", type = "TEXT",notNull = true)
    private String imagePath; // Ruta de la imagen asociada al capítulo

    @Column(name = "description", type = "TEXT", notNull = true)
    private String description;  // Descripcion de la historia

    //Constructor vacío
    public History(){}

    //Constructor
    public History(int id, String title, boolean published, String imagePath, String description) {
        this.id = id;
        this.title = title;
        this.published = published;
        this.imagePath= imagePath;
        this.description=description;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
