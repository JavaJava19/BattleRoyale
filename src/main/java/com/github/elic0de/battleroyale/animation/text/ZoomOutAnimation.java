package com.github.elic0de.battleroyale.animation.text;

import com.github.elic0de.battleroyale.animation.TextAnimation;
import lombok.NonNull;
import org.bukkit.ChatColor;

public class ZoomOutAnimation extends TextAnimation {

    public ZoomOutAnimation() {
        super("zoomOut", 1, 40);
    }

    @Override
    public String animate(@NonNull String text, long step, String... args) {
        String stripped =  ChatColor.stripColor(text);
        int length = stripped.length();
        int currentStep = getCurrentStep(step, length);
        int spaceCount = Math.min(15, currentStep);
        ChatColor color = currentStep % 2 == 0 ? ChatColor.RED : ChatColor.GOLD;

        // 空白を作成
        String space = " ".repeat(Math.max(0, spaceCount));

        String SQUARE_BRACKET_END = "]";
        String SQUARE_BRACKET_START = "[";
        String spacedText = SQUARE_BRACKET_START + space + text + space + SQUARE_BRACKET_END;
        return color + spacedText;
    }
}
