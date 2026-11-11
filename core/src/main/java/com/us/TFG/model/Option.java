package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

@Table(name = "options")
public class Option {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;

    @Column(name = "decision_id", type = "INTEGER", notNull = true) // Relación con decisiones
    private int decisionId;

    @Column(name = "text", type = "TEXT", notNull = true) // Texto de la opción
    private String text;

    @Column(name = "points_m", type = "INTEGER", defaultValue = "0") // Cambio en puntos emocionales
    private int pointsM;

    @Column(name = "points_r", type = "INTEGER", defaultValue = "0") // Cambio en puntos de relación
    private int pointsR;

    @Column(name = "next_decision_id", type = "INTEGER") // Relación con la siguiente decisión
    private Integer nextDecisionId;

    @Column(name = "required_psychological_state", type = "TEXT") // Estado psicológico requerido
    private String requiredPsychologicalState;

    // Constructor principal
    public Option(int decisionId, String text, int pointsM, int pointsR, Integer nextDecisionId, String requiredPsychologicalState) {
        this.decisionId = decisionId;
        this.text = text;
        this.pointsM = pointsM;
        this.pointsR = pointsR;
        this.nextDecisionId = nextDecisionId;
        this.requiredPsychologicalState = requiredPsychologicalState;
    }

    // Constructor vacío
    public Option() {}

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(int decisionId) {
        this.decisionId = decisionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPointsM() {
        return pointsM;
    }

    public void setPointsM(int pointsM) {
        this.pointsM = pointsM;
    }

    public int getPointsR() {
        return pointsR;
    }

    public void setPointsR(int pointsR) {
        this.pointsR = pointsR;
    }

    public Integer getNextDecisionId() {
        return nextDecisionId;
    }

    public void setNextDecisionId(Integer nextDecisionId) {
        this.nextDecisionId = nextDecisionId;
    }

    public String getRequiredPsychologicalState() {
        return requiredPsychologicalState;
    }

    public void setRequiredPsychologicalState(String requiredPsychologicalState) {
        this.requiredPsychologicalState = requiredPsychologicalState;
    }

    @Override
    public String toString() {
        return "Option{" +
            "id=" + id +
            ", decisionId=" + decisionId +
            ", text='" + text + '\'' +
            ", pointsM=" + pointsM +
            ", pointsR=" + pointsR +
            ", nextDecisionId=" + nextDecisionId +
            ", requiredPsychologicalState='" + requiredPsychologicalState + '\'' +
            '}';
    }
}
