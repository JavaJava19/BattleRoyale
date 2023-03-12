package com.github.elic0de.hungergames.animation;

import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.modifier.modifiers.GameModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;

public class Test {

    public static void animation(Collection<GameModifier> gameModifiers) {
        final List<String> frames = gameModifiers.stream().map(modifier ->  modifier.getColor() + "" + modifier.getSymbol() + "," + modifier.getColor() + modifier.getName()).toList();

        Bukkit.getOnlinePlayers().forEach(player -> {
            new BukkitRunnable() {
                int frameIndex = 0;
                int cycles = gameModifiers.size(); // Random cycle count between 3 and 5

                @Override
                public void run() {
                    final String frame[] = frames.get(frameIndex).split(",");
                    final String title = frame[0];
                    final String subtitle = frame[1];
                    player.sendTitle(title, subtitle, 0, 20, 0);

                    frameIndex++;
                    if (frameIndex >= frames.size()) {
                        frameIndex = 0;
                        cycles--;

                        if (cycles <= 0) {
                            cancel();
                            sendAnimatedTitle(player, new String[]{
                                    "&c[ Game Modifier ]",
                                    "&6[  Game Modifier  ]",
                                    "&c[   Game Modifier   ]",
                                    "&6[    Game Modifier    ]",
                                    "&c[     Game Modifier     ]",
                                    "&6[      Game Modifier      ]",
                                    "&c[       Game Modifier       ]",
                                    "&6[        Game Modifier        ]",
                                    "&c[         Game Modifier         ]",
                                    "&6[          Game Modifier          ]",
                                    "&c[           Game Modifier           ]",
                                    "&6[            Game Modifier            ]",
                                    "&c[             Game Modifier             ]",
                                    "&c[               Game Modifier               ]",
                                    "&6[              Game Modifier              ]",
                                    "&c[             Game Modifier             ]",
                                    "&6[            Game Modifier            ]",
                                    "&c[           Game Modifier           ]",
                                    "&6[          Game Modifier          ]",
                                    "&c[         Game Modifier         ]",
                                    "&6[        Game Modifier        ]",
                                    "&c[       Game Modifier       ]",
                                    "&6[      Game Modifier      ]",
                                    "&c[     Game Modifier     ]",
                                    "&6[    Game Modifier    ]",
                                    "&c[   Game Modifier   ]",
                                    "&6[  Game Modifier  ]",
                                    "&c[ Game Modifier ]",
                                    "&6[Game Modifier]",
                            }, 3);
                        }
                    }
                }
            }.runTaskTimer(HungerGames.getInstance(), 0L, 5L);
        });
    }

    private static void sendAnimatedTitle(Player player, String[] frames, int delay) {
        new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', frames[frame]), "A game modifier is being picked!", 0, 70, 0);
                //player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f);
                frame++;

                if (frame == frames.length) {
                    cancel();

                }
            }
        }.runTaskTimer(HungerGames.getInstance(), 0L, delay);
    }
}
