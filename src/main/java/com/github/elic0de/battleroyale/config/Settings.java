package com.github.elic0de.battleroyale.config;

import com.github.elic0de.battleroyale.game.GameType;
import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;

@YamlFile
public class Settings {

    @YamlComment("開始までの人数")
    public int StartPeopleNum;

    @YamlComment("ゲーム時間")
    public int AutoGameTime; // ゲーム時間

    @YamlComment("ボーダーサイズ")
    public int BorderSize; // ボーダーのサイズ

    @YamlComment("人数が揃ってゲームが始まるまでの時間")
    public int CountDownTime; // 人数がそろってゲームが始まるまでの時間

    @YamlComment("ゲームが終わって次のゲームに行くまでの時間")
    public int CoolTime; // ゲームが終わって次のゲームに行くまでの時間

    @YamlComment("ソロやチームをゲームタイプ 例: ")
    public GameType gameType;

}
