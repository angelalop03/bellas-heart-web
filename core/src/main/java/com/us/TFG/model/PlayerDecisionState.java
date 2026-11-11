package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

@Table(name = "player_decision_state")
public class PlayerDecisionState {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;

    @Column(name = "player_id", type = "INTEGER", notNull = true) // Relación con el jugador
    private int playerId;

    @Column(name = "game_id", type = "INTEGER", notNull = true) // Relación con el juego
    private int gameId;

    @Column(name = "decision_id", type = "INTEGER", notNull = true) // Relación con la decisión
    private int decisionId;

    @Column(name = "option_id", type = "INTEGER", notNull = true) // Relación con la opción de decisión
    private int optionId;

    @Column(name = "completed", type = "BOOLEAN", notNull = true, defaultValue = "false") // Si la decisión fue completada
    private boolean completed;

    // Constructor principal
    public PlayerDecisionState(int playerId, int gameId, int decisionId, int optionId, boolean completed) {
        this.playerId = playerId;
        this.gameId = gameId;
        this.decisionId = decisionId;
        this.optionId = optionId;
        this.completed = completed;
    }

    // Constructor vacío
    public PlayerDecisionState() {}

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(int decisionId) {
        this.decisionId = decisionId;
    }
    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "PlayerDecisionState{" +
            "id=" + id +
            ", playerId=" + playerId +
            ", gameId=" + gameId +
            ", decisionId=" + decisionId +
            ", optionId=" + optionId +
            ", completed=" + completed +
            '}';
    }
}
