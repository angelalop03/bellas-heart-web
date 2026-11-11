package com.us.TFG.model;

public enum Authorities {
    ADMIN(1),
    PLAYER(2);

    private final int value;

    Authorities(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Authorities fromValue(int value) {
        for (Authorities authority : Authorities.values()) {
            if (authority.getValue() == value) {
                return authority;
            }
        }
        throw new IllegalArgumentException("Valor de autoridad desconocido: " + value);
    }
}
