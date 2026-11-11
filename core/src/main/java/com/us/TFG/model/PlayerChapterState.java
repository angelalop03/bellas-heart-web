package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

@Table(name = "player_chapter_state")
public class PlayerChapterState {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;

    @Column(name = "player_id", type = "INTEGER", notNull = true)
    private int playerId; // ID del jugador

    @Column(name = "chapter_id", type = "INTEGER", notNull = true)
    private int chapterId; // ID del capítulo asociado

    @Column(name = "game_id", type = "INTEGER", notNull = true)
    private int gameId; // ID del juego asociado

    @Column(name = "played", type = "BOOLEAN", notNull = true, defaultValue = "false")
    private boolean played; // Indica si el capítulo ha sido jugado

    @Column(name = "unlocked", type = "BOOLEAN", notNull = true, defaultValue = "false")
    private boolean unlocked; // Indica si el capítulo está desbloqueado

    // Constructor principal
    public PlayerChapterState(int playerId, int chapterId, int gameId, boolean played, boolean unlocked) {
        this.playerId = playerId;
        this.chapterId = chapterId;
        this.gameId = gameId;
        this.played = played;
        this.unlocked = unlocked;
    }

    // Constructor vacío
    public PlayerChapterState() {}

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

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    @Override
    public String toString() {
        return "PlayerChapterState{" +
            "id=" + id +
            ", playerId=" + playerId +
            ", chapterId=" + chapterId +
            ", gameId=" + gameId +
            ", played=" + played +
            ", unlocked=" + unlocked +
            '}';
    }
}
