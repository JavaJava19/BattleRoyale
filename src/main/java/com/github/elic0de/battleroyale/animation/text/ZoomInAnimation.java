package com.github.elic0de.battleroyale.animation.text;

import com.github.elic0de.battleroyale.animation.TextAnimation;
import lombok.NonNull;

public class ZoomInAnimation extends TextAnimation {

    public ZoomInAnimation() {
        super("zooIn", 1, 40);
    }

    @Override
    public String animate(@NonNull String text, long step, String... args) {
        int currentStep = getCurrentStep(step, 10);
        int spaceCount = Math.max(0, 10 - currentStep);
        // 空白を作成

        String space = " ".repeat(spaceCount);
        String SQUARE_BRACKET_END = "]";
        String SQUARE_BRACKET_START = "[";
        return SQUARE_BRACKET_START + space + text + space + SQUARE_BRACKET_END;
    }
}