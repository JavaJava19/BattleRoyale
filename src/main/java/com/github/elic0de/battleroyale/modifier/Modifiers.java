package com.github.elic0de.battleroyale.modifier;

public enum Modifiers {
    TRUE_UHC("uhc"),
    MAGIC_POWER("magic"),
    FLOWER_POWER("flower_power"),
    HEALTH_ON_KILL("health_on_kill"),
    NIGHT_TIME("night_time"),
    PERALS("pearls");

    private final String key;


    Modifiers(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}