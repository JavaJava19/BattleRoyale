package com.github.elic0de.hungergames.modifier;

public enum Modifiers {
    GET_A_IRON_INGOT("ironIngot");

    private final String key;


    Modifiers(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}