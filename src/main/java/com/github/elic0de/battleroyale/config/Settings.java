package com.github.elic0de.battleroyale.config;

import com.github.elic0de.battleroyale.game.GameType;
import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;

@YamlFile
public class Settings {

    @YamlComment("開始までの人数")
    public int minPlayers;

    @YamlComment("ボーダーサイズ")
    public int borderSize; // ボーダーのサイズ

    @YamlComment("人数が揃ってゲームが始まるまでの時間")
    public int countdownTime; // 人数がそろってゲームが始まるまでの時間

    @YamlComment("ゲーム時間")
    public int gameTime; // ゲーム時間

    @YamlComment("ゲームが終わって次のゲームに行くまでの時間")
    public int nextGameTime; // ゲームが終わって次のゲームに行くまでの時間

    @YamlComment("ソロやチームのゲームタイプ 例: ")
    public GameType gameType;

}
