package com.github.elic0de.hungergames.modifier;

import com.github.elic0de.hungergames.animation.Test;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.modifier.modifiers.GameModifier;
import com.github.elic0de.hungergames.modifier.modifiers.TrueUHC;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModifierManager {


    private final HungerGame game;
    private final Random random = new Random();
    private GameModifier currentModifier;

    private final Map<String, GameModifier> modifiers = new HashMap<>();

    public ModifierManager(HungerGame game) {
        this.game = game;
        registerMissions();
    }

    private void registerMissions() {
        modifiers.put(Modifiers.GET_A_IRON_INGOT.getKey(), new TrueUHC());
    }

    public void modify() {
        Test.animation(getModifiers());
    }

    public void reset() {
        currentModifier = null;
    }

    private void randomModifier() {
        getRandomModifier().ifPresent(mission -> currentModifier = mission);
    }

    private GameModifier getCurrentMission() {
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
