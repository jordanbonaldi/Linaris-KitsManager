
package net.neferett.LinarisKits.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.neferett.LinarisKits.Managers.UsersManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameAPI {
    private GameType m_game;
    private HashMap<UUID, Kit> m_kits = new HashMap();

    public GameAPI(GameType game) {
        this.m_game = game;
    }

    public int getKitInventorySlots() {
        return Math.max(9, (LinarisKitsAPI.getKitsForGame(this.m_game).size() + 8) / 9 * 9);
    }

    public Inventory getKitInventory(Player player) {
        List<Kit> kits = LinarisKitsAPI.getKitsForGame(this.m_game);
        int slots = this.getKitInventorySlots();
        Inventory inv = Bukkit.createInventory((InventoryHolder)player, (int)slots, "Choisir un kit");
        User user = UsersManager.getInstance().getUser(player);
        int i = 0;
        while (i < kits.size()) {
            int level = user.getLevelForKit(kits.get(i));
            ItemStack item = kits.get(i).getItem();
            ItemMeta meta = item.getItemMeta();
            LinkedList<String> lore = new LinkedList<String>();
            lore.add((Object)ChatColor.RESET + kits.get(i).getDescription());
            lore.add("");
            lore.add("§r§6Niveau actuel : §e" + level + "/5");
            if (level == 0) {
                lore.add("§r§cNon possédé");
            } else {
                lore.addAll(kits.get(i).getLevel(level).getLore());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, kits.get(i).getItem());
            ++i;
        }
        return inv;
    }

    private int getRate(Player player) {
        int rate = 1;
        if (player.hasPermission("kit.vipelite")) {
            rate = 4;
        } else if (player.hasPermission("kit.megavip")) {
            rate = 3;
        } else if (player.hasPermission("kit.vip")) {
            rate = 2;
        }
        return rate;
    }

    public void kill(Player player) {
        int rate = this.getRate(player);
        UsersManager.getInstance().getUser(player).addCoins(rate);
    }

    public void win(List<Player> players) {
        for (Player player : players) {
            int rate = this.getRate(player);
            UsersManager.getInstance().getUser(player).addCoins(10 *rate);
        }
    }
    
    public void winHG(Player player){
            int rate = this.getRate(player);
            UsersManager.getInstance().getUser(player).addCoins(10 * rate);
        }
    	
    

    public void onInventoryClick(InventoryClickEvent event) {
        Kit kit;
        Player player = (Player)event.getWhoClicked();
        Inventory inv = event.getInventory();
        if (inv.getTitle().equalsIgnoreCase("Choisir un kit") && event.getRawSlot() < this.getKitInventorySlots() && (kit = LinarisKitsAPI.getKit(this.m_game, event.getCurrentItem())) != null) {
            if (UsersManager.getInstance().getUser(player).getLevelForKit(kit) <= 0) {
                player.sendMessage("§cVous n'avez pas débloquer ce kit");
            } else {
                this.m_kits.put(player.getUniqueId(), kit);
                player.sendMessage("§aVous avez choisi le kit §6<kit> §a!".replaceAll("<kit>", kit.getName()));
            }
        }
        event.setCancelled(true);
        player.closeInventory();
    }

    public void applyKit(Player player) {
        if (!this.m_kits.containsKey(player.getUniqueId())) {
            return;
        }
        this.m_kits.get(player.getUniqueId()).apply(player);
    }
}

