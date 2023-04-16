package com.github.elic0de.battleroyale.kit;

public class KitManager {

    private static KitManager instance;

    public static void load(){
        instance = new KitManager();
    }

    public static KitManager getInstance(){
        return instance;
    }

    private final HashSet<Player> cooldownPlayers = new HashSet<>();

    private final Uhcffa plugin = Uhcffa.getInstance();

    private final SpectetorSet spectetorSet = SpectetorSet.getInstance();

    private final UserSet userSet = UserSet.getInstnace();

    //キットデータを保存するフォルダー
    public final File folder = new File(plugin.getDataFolder() + File.separator + "kits");

    private final Map<String, Kit> kits = new HashMap<>();

    private final HashMap<Player, Kit> selectedPlayer = new HashMap<>();

    private KitManager() {
        //フォルダーが存在しなければ作成する
        if(!folder.exists()) folder.mkdirs();
        //各コンフィグ毎に処理をする
        for(File file : Optional.ofNullable(folder.listFiles()).orElse(new File[0])){
            if(file.isFile()) {
                String fileName = file.getName();
                //拡張子を削除してキット名を取得する
                String gameName = fileName.substring(0, fileName.length() - 4);

                //キットを登録する
                registerKit(gameName);
            }
        }
    }

    public void registerKit(String kitName){
        File file = new File(folder, kitName + ".yml");

        //コンフィグが存在しなければ戻る
        if(!file.exists()) return;

        //コンフィグを取得する
        Yaml yaml = makeYaml(kitName);

        //コンフィグに基づきアスレを生成する
        Kit kit =  new Kit(yaml);
        kits.put(kitName, kit);

    }

    public void registerKit(Kit kit){
        final String kitName = kit.getName();
        //既にアリーナデータが存在するのであれば戻る
        if(kits.containsKey(kitName)) return;
        //アリーナデータコンフィグ作成する
        makeYaml(kitName);
        //登録する
        kits.put(kit.getName(), kit);
    }

    public void removeKit(String kitName) {
        makeYaml(kitName).file.delete();
        kits.remove(kitName);
    }

    public void saveAll(){
        kits.values().forEach(Kit::save);
    }

    public void reload(){
        kits.values().forEach(Kit::save);
        kits.clear();
        load();
    }

    public void selectToKit(Player player, Kit kit, boolean isEdit) {
        User user = userSet.getUser(player);

        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setHelmet(new ItemStack(Material.AIR));
        inv.setChestplate(new ItemStack(Material.AIR));
        inv.setLeggings(new ItemStack(Material.AIR));
        inv.setBoots(new ItemStack(Material.AIR));
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setMaxHealth(40);
        player.setHealth(player.getMaxHealth());

        /*if(!isEdit) {
            if (user.isEditedKit(kit.getName())) {
                Sync.define(() -> user.apply(kit)).execute();
            } else {
                Sync.define(() -> kit.apply(player)).execute();
            }
        }*/
        selectedPlayer.put(player, kit);

        if(isEdit) {
            Sync.define(() -> kit.apply(player)).execute();
            player.teleport(Uhcffa.getInstance().config().getLocation());
            player.sendMessage("/kitsaveでキットを保存できます");
        } else {
            player.setAllowFlight(false);
            Sync.define(() -> {
                CustomPlayer customPlayer = Uhcffa.getCustomPlayer(PlayerConverter.getID(player));
                customPlayer.cancelRequest();

                final CustomPlayer.Preset preset = customPlayer.getPreset();
                if (preset == null) {
                    ItemStack[] armor = new ItemStack[4];

                    armor[0] = ItemBuilder.of(Material.IRON_BOOTS).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
                    armor[1] = ItemBuilder.of(Material.IRON_LEGGINGS).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
                    armor[2] = ItemBuilder.of(Material.DIAMOND_CHESTPLATE).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build();
                    armor[3] = ItemBuilder.of(Material.DIAMOND_HELMET).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();

                    inv.setArmorContents(armor);

                    inv.setItem(0, ItemBuilder.of(Material.DIAMOND_SWORD).build());
                    inv.setItem(1, ItemBuilder.of(Material.FISHING_ROD).build());
                    inv.setItem(2, ItemBuilder.of(Material.BOW).build());
                    inv.setItem(3, ItemBuilder.of(Material.ARROW).amount(8).build());
                    ExecutableItemType.LIGHT_APPLE.setItem(4, player);
                    inv.setItem(8, ItemBuilder.of(Material.COBBLESTONE).amount(64).build());
                    ExecutableItemType.FFA_SHOP_ITEM.setItem(5,player);
                    ExecutableItemType.RESPAWN_ITEM.setItem(6,player);

                    customPlayer.setPreset();
                } else {
                    preset.applyContent();
                }

                for(int i = 0; i< player.getInventory().getSize()-1; ++i) {
                    ItemStack item = player.getInventory().getItem(i);
                    if(item == null) continue;
                    if(item.getType().equals(Material.COBBLESTONE)) {
                        int finalI = i;
                        Blocks.BLOCKS.stream().filter(kit1 -> kit1.item == user.blockItem).forEach(kit1 -> {
                            ItemStack block = kit1.item.clone();
                            if (block.hasItemMeta()) {
                                ItemMeta im = block.getItemMeta();
                                if (im.hasLore())
                                    im.setLore(new ArrayList<>());
                                if (im.hasDisplayName())
                                    im.setDisplayName("");
                                block.setItemMeta(im);
                            }
                            block.setAmount(64);
                            player.getInventory().setItem(finalI, block);
                        });
                    }
                }

                player.teleport(Uhcffa.getInstance().config().getSpawnPoints().stream().skip((new Random()).nextInt(Uhcffa.getInstance().config().getSpawnPoints().size())).findFirst().get());
                spectetorSet.applyShowMode(player);
            }).executeLater(2*20);
        }
        player.setGameMode(GameMode.SURVIVAL);
        Uhcffa.getCustomPlayer(player).sendTranslated("ffa.join");
    }

    public Collection<Kit> getKits(){
        return kits.values();
    }


    public Kit getKit(String kitName){
        return kits.get(kitName);
    }

    public HashMap<Player, Kit> getSelectedPlayer() {
        return selectedPlayer;
    }

    public void removeSelectedKit(Player player) {
        selectedPlayer.remove(player);
    }

    public HashSet<Player> getCooldownPlayers() {
        return cooldownPlayers;
    }

    public boolean isSelected(Player player) {
        return selectedPlayer.containsKey(player);
    }

    public boolean containsKit(Kit kit){
        return containsKit(kit.getName());
    }

    public boolean containsKit(String kitName){
        return kits.containsKey(kitName);
    }

    public Yaml makeYaml(String kitName){
        return new Yaml(plugin, new File(folder, kitName + ".yml"), "kit.yml");
    }
}
