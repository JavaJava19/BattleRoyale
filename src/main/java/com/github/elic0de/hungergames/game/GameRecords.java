package com.github.elic0de.hungergames.game;

import com.github.elic0de.hungergames.user.GameUser;
import de.themoep.minedown.MineDown;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameRecords {

    // 全記録
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
            final long kills = records.get(uuid) + 1;
            records.put(uuid, kills);
            sortAsync();

            if (kills >= 3) {
                if (killLeader != null) {
                    if (getRank(user) == 1) {
                        if (killLeader != uuid) {
                            game.broadcast(new MineDown(String.format("&c%s&rが&c%s&rキルで新しいキルリーダになりました", user.getUsername(), Math.toIntExact(kills))));
                            killLeader = uuid;
                        }
                    }
                } else {
                    game.broadcast(new MineDown(String.format("&c%s&rが&c3&rキルで新しいキルリーダになりました", user.getUsername())));
                    killLeader = uuid;
                }
            }

            return;
        }
        records.put(uuid, 1L);
        sortAsync();
    }

    private boolean containsRecord(GameUser user){
        return records.containsKey(user.getUniqueId());
    }

    public long personalBest(UUID uuid){
        return records.getOrDefault(uuid, 0L);
    }

    public void removeAllRecord() {
        //すべてのレコードを削除する
        records.clear();
        sortAsync();
    }

    private void sortAsync(){
        final List<Map.Entry<UUID, Long>> list = new ArrayList<>(records.entrySet());
        final AtomicInteger position = new AtomicInteger();

        // 記録を降順にソートする
        // 最大で上位10件の記録をリストに追加する
        list.stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEach(entry -> {
            rank.put(entry.getKey(), position.incrementAndGet());
        });
    }

    public Map<UUID, Integer> getRank() {
        return rank;
    }
}
