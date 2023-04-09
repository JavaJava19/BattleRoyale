package com.github.elic0de.battleroyale.modifier;

import com.github.elic0de.battleroyale.animation.Test;
import com.github.elic0de.battleroyale.game.Game;
import com.github.elic0de.battleroyale.modifier.modifiers.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModifierManager {


    private final Game game;
    private final Random random = new Random();
    private GameModifier currentModifier;

    private final Map<String, GameModifier> modifiers = new HashMap<>();

    public ModifierManager(Game game) {
        this.game = game;
        registerMissions();
    }

    private void registerMissions() {
        modifiers.put(Modifiers.TRUE_UHC.getKey(), new TrueUHC());
        modifiers.put(Modifiers.MAGIC_POWER.getKey(), new MagicPower());
        modifiers.put(Modifiers.FLOWER_POWER.getKey(), new FlowerPower());
        modifiers.put(Modifiers.PERALS.getKey(), new Pearls());
        modifiers.put(Modifiers.NIGHT_TIME.getKey(), new NightTime());
        modifiers.put(Modifiers.HEALTH_ON_KILL.getKey(), new HealthOnKill());
    }

    public void modify() {
        new Test().animation(getModifiers());
        getCurrentMission().modify();
    }

    public void reset() {
        currentModifier = null;
    }

    private void randomModifier() {
        getRandomModifier().ifPresent(mission -> currentModifier = mission);
    }

    public GameModifier getCurrentMission() {
        if (currentModifier == null) {
            randomModifier();
        }
        return currentModifier;
    }

    private Optional<GameModifier> getRandomModifier() {
        Optional<Modifiers> missions = Arrays.stream(Modifiers.values()).skip(random.nextInt(Modifiers.values().length)).findFirst();
        return missions.map(value -> getModifier(value.getKey()));
    }

    @Nullable
    public GameModifier getModifier(String key) {
        return this.modifiers.get(key);
    }

    public Collection<GameModifier> getModifiers() {
        return modifiers.values();
    }
}
