package com.github.elic0de.battleroyale.user;

import com.github.elic0de.battleroyale.utils.annoData.DatabaseMySQL;
import com.github.elic0de.battleroyale.utils.annoData.Identification;
import lombok.Data;

@DatabaseMySQL(table = "kos")
@Data
public class GameUserData {

    @Identification
    private String uniqueId;

    private int level;

    private int kills;

    private double xp;

    public int coins;

}
