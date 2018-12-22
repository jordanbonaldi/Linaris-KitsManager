package net.neferett.LinarisKits.Managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.neferett.LinarisKits.api.GameType;
import net.neferett.LinarisKits.api.Kit;
import net.neferett.LinarisKits.api.KitLevel;
import net.neferett.LinarisKits.api.LinarisKitsAPI;
import net.neferett.LinarisKits.api.User;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class KitsManager {
    private List<Kit> m_kits = new LinkedList<Kit>();
    private static KitsManager instance;

    public static KitsManager getInstance() {
        if (instance == null) {
            instance = new KitsManager();
        }
        return instance;
    }

    private KitsManager() {
        try {
            PreparedStatement state = DatabaseManager.prepareStatement("SELECT * FROM linariskits_kits");
            ResultSet result = state.executeQuery();
            while (result.next()) {
                try {
                    int id = result.getInt("id");
                    String name = result.getString("name");
                    String perm = result.getString("permission");
                    GameType game = GameType.valueOf(result.getString("game").toUpperCase());
                    String desc = result.getString("desc");
                    ItemStack item = LinarisKitsAPI.toItemStack(result.getString("item"));
                    KitLevel[] levels = new KitLevel[5];
                    PreparedStatement state_lvl = DatabaseManager.prepareStatement("SELECT * FROM linariskits_kitslevels WHERE kit_id=? ORDER BY `level`");
                    state_lvl.setInt(1, id);
                    ResultSet result_lvl = state_lvl.executeQuery();
                    while (result_lvl.next()) {
                        int price = result_lvl.getInt("price");
                        List<String> lore = Arrays.asList(result_lvl.getString("lore").split("#"));
                        String[] items_str = result_lvl.getString("items").split("#");
                        LinkedList<LinarisKitsAPI.ItemStackAndSlot> items = new LinkedList<LinarisKitsAPI.ItemStackAndSlot>();
                        String[] arrstring = items_str;
                        int n = arrstring.length;
                        int n2 = 0;
                        while (n2 < n) {
                            String item_str = arrstring[n2];
                            items.add(LinarisKitsAPI.toItemStackAndSlot(item_str));
                            ++n2;
                        }
                        levels[result_lvl.getInt("level") - 1] = new CraftKitLevel(price, lore, items);
                    }
                    this.m_kits.add(new CraftKit(this, id, name, desc, perm, game, item, levels));
                    continue;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            state.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addKit(String name, String desc, String perm, GameType game, ItemStack item, KitLevel[] levels) {
        try {
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO `").append("linariskits_kits").append("` ");
            query.append("(`name`, `game`, `desc`, `item`, `permission`) VALUES(?, ?, ?, ?, ?);");
            PreparedStatement state = DatabaseManager.prepareStatement(query.toString());
            state.setString(1, name);
            state.setString(2, game.name());
            state.setString(3, desc);
            state.setString(4, LinarisKitsAPI.toString(item));
            state.setString(5, perm);
            state.executeUpdate();
            state.close();
            ResultSet result_id = DatabaseManager.prepareStatement("SELECT LAST_INSERT_ID() AS id;").executeQuery();
            result_id.next();
            int kit_id = result_id.getInt("id");
            query = new StringBuilder();
            query.append("INSERT INTO `").append("linariskits_kitslevels");
            query.append("` (`kit_id`, `level`, `price`, `lore`, `items`) VALUES");
            int i = 0;
            while (i < levels.length) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append("(?, ?, ?, ?, ?)");
                ++i;
            }
            query.append(";");
            state = DatabaseManager.prepareStatement(query.toString());
            i = 0;
            while (i < levels.length) {
                StringBuilder lore = new StringBuilder();
                for (String line : levels[i].getLore()) {
                    if (!lore.toString().isEmpty()) {
                        lore.append("#");
                    }
                    lore.append(line);
                }
                StringBuilder items = new StringBuilder();
                for (LinarisKitsAPI.ItemStackAndSlot is_slot : levels[i].getItems()) {
                    if (!items.toString().isEmpty()) {
                        items.append("#");
                    }
                    items.append(LinarisKitsAPI.toString(is_slot));
                }
                state.setInt(i * 5 + 1, kit_id);
                state.setInt(i * 5 + 2, i + 1);
                state.setInt(i * 5 + 3, levels[i].getPrice());
                state.setString(i * 5 + 4, lore.toString());
                state.setString(i * 5 + 5, items.toString());
                ++i;
            }
            state.executeUpdate();
            state.close();
            this.m_kits.add(new CraftKit(this, kit_id, name, desc, perm, game, item, levels));
            DatabaseManager.getInstance().update();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Kit> getKits() {
        return this.m_kits;
    }

    public List<Kit> getKitsForGame(GameType game) {
        LinkedList<Kit> kits = new LinkedList<Kit>();
        for (Kit kit : this.m_kits) {
            if (!kit.getGameType().equals((Object)game)) continue;
            kits.add(kit);
        }
        return kits;
    }

    public Kit getKit(GameType game, ItemStack is) {
        for (Kit kit : this.getKitsForGame(game)) {
            ItemStack item = kit.getItem();
            if (item.getType() != is.getType() || item.getDurability() != is.getDurability()) continue;
            return kit;
        }
        return null;
    }

    public Kit getKit(int id) {
        for (Kit kit : this.m_kits) {
            if (kit.getId() != id) continue;
            return kit;
        }
        return null;
    }

    public class CraftKit
    implements Kit {
        private int m_id;
        private String m_name;
        private String m_desc;
        private String m_perm;
        private GameType m_game;
        private ItemStack m_item;
        private KitLevel[] m_levels;
        final  KitsManager this$0;

        private CraftKit(KitsManager kitsManager, int id, String name, String desc, String perm, GameType game, ItemStack item, KitLevel[] levels) {
            this.this$0 = kitsManager;
            this.m_id = id;
            this.m_name = ChatColor.translateAlternateColorCodes((char)'&', name);
            this.m_desc = ChatColor.translateAlternateColorCodes((char)'&', desc);
            this.m_item = item;
            ItemMeta meta = this.m_item.getItemMeta();
            meta.setDisplayName(this.m_name);
            this.m_item.setItemMeta(meta);
            this.m_perm = perm;
            this.m_game = game;
            this.m_levels = levels;
        }

        @Override
        public int getId() {
            return this.m_id;
        }

        @Override
        public String getFieldName() {
            return "kit" + this.m_id;
        }

        @Override
        public String getName() {
            return this.m_name;
        }

        @Override
        public String getDescription() {
            return this.m_desc;
        }

        @Override
        public boolean hasPerm(Player player) {
            return this.m_perm == null ? true : player.hasPermission(this.m_perm);
        }

        @Override
        public GameType getGameType() {
            return this.m_game;
        }

        @Override
        public ItemStack getItem() {
            return this.m_item;
        }

        @Override
        public KitLevel getLevel(int level) {
            return this.m_levels[level - 1];
        }

        @Override
        public void apply(Player player) {
            User user = UsersManager.getInstance().getUser(player);
            PlayerInventory inv = player.getInventory();
            KitLevel level = this.getLevel(user.getLevelForKit(this));
            for (LinarisKitsAPI.ItemStackAndSlot is : level.getItems()) {
                inv.setItem(is.getSlot(), is.getItemStack());
            }
        }

    }

    public static class CraftKitLevel
    implements KitLevel {
        protected int m_price;
        protected List<String> m_lore;
        protected List<LinarisKitsAPI.ItemStackAndSlot> m_items = new LinkedList<LinarisKitsAPI.ItemStackAndSlot>();

        public CraftKitLevel(int price, List<String> lore, List<LinarisKitsAPI.ItemStackAndSlot> items) {
            this.m_price = price;
            this.m_lore = lore;
            this.m_items = items;
            int i = 0;
            while (i < this.m_lore.size()) {
                this.m_lore.set(i, (Object)ChatColor.RESET + this.m_lore.get(i));
                ++i;
            }
        }

        @Override
        public int getPrice() {
            return this.m_price;
        }

        @Override
        public List<String> getLore() {
            return this.m_lore;
        }

        @Override
        public List<LinarisKitsAPI.ItemStackAndSlot> getItems() {
            return this.m_items;
        }
    }

}

