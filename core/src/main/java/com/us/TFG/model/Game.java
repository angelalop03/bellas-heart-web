package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;
import com.us.TFG.model.RelationshipState;
import com.us.TFG.model.PsychologicalState;


import java.util.Date;

@Table(name = "games")
public class Game {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;  // ID único para la base de datos

    @Column(name = "name", type = "TEXT", notNull = true)
    private String name;  // Nombre del juego

    @Column(name = "history_id", type = "INTEGER", notNull = true)
    private Integer historyId; // ID de la historia elegida para la partida

    @Column(name = "current_chapter_id", type = "INTEGER", notNull = true, defaultValue = "0")
    private int currentChapterId;

    @Column(name = "current_scene_id", type = "INTEGER", notNull = true, defaultValue = "0")
    private int currentSceneId;

    @Column(name = "current_decision_id", type = "INTEGER", notNull = true, defaultValue = "0")
    private int currentDecisionId;

    @Column(name = "player_id", type = "INTEGER", notNull = true)
    private int playerId;

    @Column(name = "start_date", type = "DATE", notNull = true)
    private Date startDate;  // Fecha de inicio de la partida

    @Column(name = "state", type = "TEXT", notNull = true, defaultValue = "'IN_PROGRESS'")
    private GameState state;  // Estado del juego

    @Column(name = "points_m", type = "INTEGER", notNull = true, defaultValue = "0")
    private int pointsM; // Puntos emocionales/morales

    @Column(name = "points_r", type = "INTEGER", notNull = true, defaultValue = "0")
    private int pointsR; // Puntos de relación

    // Constructor principal
    public Game(String name, int playerId,Integer historyId) {
        this.name = name;
        this.playerId = playerId;
        this.historyId = historyId;
        this.startDate = new Date();
        this.state = GameState.IN_PROGRESS;
        this.pointsM = 0;
        this.pointsR = 0;
        this.currentChapterId = 0;
        this.currentSceneId = 0;
        this.currentDecisionId = 0;
    }

    // Constructor vacío
    public Game() {}

    // Métodos para calcular los estados basados en los puntos
    public PsychologicalState getPsychologicalState() {
        if (pointsM >= 0 && pointsM < 25) {
            return PsychologicalState.STABLE;
        } else if (pointsM >= 25 && pointsM < 50) {
            return PsychologicalState.SLIGHTLY_ANXIOUS;
        } else if (pointsM >= 50 && pointsM < 75) {
            return PsychologicalState.ANXIOUS;
        } else {
            return PsychologicalState.DISTRESSED;
        }
    }

    public RelationshipState getRelationshipState() {
        if (pointsR >= 0 && pointsR < 30) {
            return RelationshipState.CONFIDENT;
        } else if (pointsR >= 30 && pointsR < 60) {
            return RelationshipState.LOW_TENSION;
        } else if (pointsR >= 60 && pointsR < 80) {
            return RelationshipState.HIGH_TENSION;
        } else {
            return RelationshipState.CRITICAL;
        }
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }
    public int getCurrentChapterId() {
        return currentChapterId;
    }

    public void setCurrentChapterId(int currentChapterId) {
        this.currentChapterId = currentChapterId;
    }

    public int getCurrentSceneId() {
        return currentSceneId;
    }

    public void setCurrentSceneId(int currentSceneId) {
        this.currentSceneId = currentSceneId;
    }

    public int getCurrentDecisionId() {
        return currentDecisionId;
    }

    public void setCurrentDecisionId(int currentDecisionId) {
        this.currentDecisionId = currentDecisionId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
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

    @Override
    public String toString() {
        return "Game{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", historyId=" + historyId +
            ", currentChapterId=" + currentChapterId +
            ", currentSceneId=" + currentSceneId +
            ", currentDecisionId=" + currentDecisionId +
            ", playerId=" + playerId +
            ", startDate=" + startDate +
            ", state=" + state +
            ", pointsM=" + pointsM +
            ", pointsR=" + pointsR +
            ", psychologicalState=" + getPsychologicalState() +
            ", relationshipState=" + getRelationshipState() +
            '}';
    }
}
