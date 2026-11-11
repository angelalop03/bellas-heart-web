package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

import java.util.List;

@Table(name = "decisions")
public class Decision {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;

    @Column(name = "scene_id", type = "INTEGER", notNull = true) // Relación con escenas
    private int sceneId;

    @Column(name = "decision_order", type = "INTEGER", notNull = true) // Orden de la decisión en la escena
    private int decisionOrder;

    @Column(name = "character_id", type = "INTEGER") // Relación con personajes
    private int characterId;

    @Column(name = "dialogue", type = "TEXT") // Texto del diálogo del personaje
    private String dialogue;

    // Constructor principal
    public Decision(int id,int sceneId, int decisionOrder, int characterId, String dialogue) {
        this.id=id;
        this.sceneId = sceneId;
        this.decisionOrder = decisionOrder;
        this.characterId = characterId;
        this.dialogue = dialogue;
    }

    // Constructor vacío
    public Decision() {}

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public int getDecisionOrder() {
        return decisionOrder;
    }

    public void setDecisionOrder(int decisionOrder) {
        this.decisionOrder = decisionOrder;
    }

    public int getCharacterId() {
        return characterId;
    }

    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    public String getDialogue() {
        return dialogue;
    }

    public void setDialogue(String dialogue) {
        this.dialogue = dialogue;
    }


    @Override
    public String toString() {
        return "Decision{" +
            "id=" + id +
            ", sceneId=" + sceneId +
            ", decisionOrder=" + decisionOrder +
            ", characterId=" + characterId +
            ", dialogue='" + dialogue + '\'' +
            '}';
    }
}
