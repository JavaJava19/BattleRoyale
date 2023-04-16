package com.github.elic0de.battleroyale.scoreboard;

import com.github.elic0de.battleroyale.user.GameUser;
import fr.mrmicky.fastboard.FastBoard;

public class GamePlayerScoreboard {

    private final GameUser player;
    private FastBoard board;

    public GamePlayerScoreboard(GameUser player) {
        this.player = player;
        this.board = new FastBoard(player.getPlayer());
        board.updateTitle("+ --- TheJpsPit--- +");
    }

    public void show() {
        hide();
        this.board = new FastBoard(player.getPlayer());
        board.updateTitle("+ --- TheJpsPit--- +");
        init();
    }

    public void hide() {
        if (board != null) {
            if (board.isDeleted()) return;
            board.delete();
        }
    }

    public void init() {
        board.updateLines(
                " ",
                "キル:",
                "No Data",
                "No Data",
                "No Data",
                " ",
                "レート：",
                "No Data",
                "No Data",
                "No Data",
                " ",
                "+ ---------------- +"
        );
    }

    public void reset() {
        board.updateLines("");
    }

    public void updateRound(String string) {
        board.updateLine(0, string.replaceAll("start", ""));
    }
}
