package com.github.elic0de.hungergames.game;

import com.github.elic0de.hungergames.user.GameUser;
import de.themoep.minedown.MineDown;

import java.util.*;

public class GameRecords {

    //全記録
    private final Map<UUID, Long> records = new HashMap<>();

    private final HungerGame game;

    private Map<UUID, Integer> rank = new HashMap<>();

    private UUID killLeader;

    public GameRecords(HungerGame game) {
        this.game = game;
        sortAsync();
    }

    public int getRank(GameUser user) {
        final UUID uuid = user.getUniqueId();
        if(rank.containsKey(uuid)) return rank.get(uuid);
        return -1;
    }

    public void addKill(GameUser user){
        final UUID uuid = user.getUniqueId();
        if (containsRecord(user)) {
            final long kills = records.get(uuid);

            if (kills >= 3) {
                if (killLeader != null) {
                    if (getRank(user) == 1) {
                        if (killLeader != uuid) {
                            game.broadcast(new MineDown(String.format("&c%s&rが&c%s&rキルで新しいキルリーダになりました", user.getUsername(), Math.toIntExact(kills))));
                            killLeader = uuid;
                        }
                    }
                } else {
                    game.broadcast(new MineDown(String.format("&c%s&rが&c3キルで新しいキルリーダになりました", user.getUsername())));
                    killLeader = uuid;
                }
            }

            records.put(uuid, kills + 1);
            return;
        }
        records.put(uuid, 1L);
        sortAsync();
    }

    private boolean containsRecord(GameUser user){
        return records.containsKey(user.getUniqueId());
    }

    public long personalBest(GameUser user){
        return records.getOrDefault(user.getUniqueId(), 0L);
    }

    public void removeAllRecord() {
        //すべてのレコードを削除する
        records.clear();
        sortAsync();
    }

    public void sortAsync(){
        final List<Map.Entry<UUID, Long>> list = new ArrayList<>(records.entrySet());

        //記録を昇順にソートする
        list.sort(Map.Entry.comparingByValue());

        //最大で上位10件の記録をリストに追加する
        for(int index = 0; index < records.size(); index++){
            //ソート済みリストから記録を取得する
            Map.Entry<UUID, Long> record = list.get(index);

            UUID uuid = record.getKey();
            rank.put(uuid,index + 1);
        }
    }
}
