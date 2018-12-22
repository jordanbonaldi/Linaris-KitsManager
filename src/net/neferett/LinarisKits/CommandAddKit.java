package net.neferett.LinarisKits;

import java.util.LinkedList;
import java.util.List;

import net.neferett.LinarisKits.Managers.KitsManager;
import net.neferett.LinarisKits.api.GameType;
import net.neferett.LinarisKits.api.KitLevel;
import net.neferett.LinarisKits.api.LinarisKitsAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CommandAddKit
  implements CommandExecutor, Listener
{
  private List<KitAdder> m_adders = new LinkedList();

  public CommandAddKit() {
    Bukkit.getPluginManager().registerEvents(this, LinarisKits.getInstance());
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (!sender.isOp()) {
      sender.sendMessage("§cVous devez être administrateur !");
      return true;
    }
    if (!(sender instanceof Player)) {
      sender.sendMessage("§cConnectez vous en jeu");
      return true;
    }
    if (args.length < 2) return false;

    Player player = (Player)sender;
    try {
      StringBuilder name = new StringBuilder();
      for (int i = 1; i < args.length; i++) {
        if (i > 1) name.append(" ");
        name.append(args[i]);
      }

      this.m_adders.add(new KitAdder(player, GameType.valueOf(args[0]), name.toString()));
    } catch (Exception e) {
      player.sendMessage("§cJeu inconnu :");
      for (GameType game : GameType.values()) {
        player.sendMessage("§c - " + game.name());
      }
      player.sendMessage("§c--------");
    }
    return true;
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    KitAdder adder = getKitAdder(event.getPlayer());
    if (adder == null) return;

    if (adder.getState() == AddKitState.DESCRIPTION) {
      adder.desc = event.getMessage();
      adder.setState(AddKitState.ITEM);
      event.setCancelled(true);
    }
    else if (adder.getState() == AddKitState.LEVEL_PRICE) {
      try {
        adder.levels.add(new KitLevelAdd(Integer.parseInt(event.getMessage())));
        adder.setState(AddKitState.LEVEL_LORE);
        event.setCancelled(true); } catch (Exception e) {
        e.printStackTrace(); adder.setState(AddKitState.LEVEL_PRICE);
      }
    } else if (adder.getState() == AddKitState.LEVEL_LORE) {
      if (event.getMessage().equalsIgnoreCase("ok")) {
        adder.setState(AddKitState.LEVEL_ITEMS);
      }
      else {
        KitLevelAdd level = (KitLevelAdd)adder.levels.get(adder.levels.size() - 1);
        level.getLore().add(event.getMessage());
        level.setLore(level.getLore());
      }
      event.setCancelled(true);
    }
    else if (adder.getState() == AddKitState.LEVEL_ITEMS) {
      if (event.getMessage().equalsIgnoreCase("ok")) {
        Inventory inv = event.getPlayer().getInventory();
        List items = new LinkedList();
        for (int i = 0; i < 36; i++) {
          ItemStack is = inv.getItem(i);
          if ((is != null) && (is.getType() != Material.AIR)) {
            items.add(new LinarisKitsAPI.ItemStackAndSlot(is, i));
          }
        }

        ((KitLevelAdd)adder.levels.get(adder.levels.size() - 1)).setItems(items);

        if (adder.levels.size() >= 5) {
          adder.setState(AddKitState.PERMISSION);
        }
        else {
          adder.setState(AddKitState.LEVEL_PRICE);
        }
        event.setCancelled(true);
      }
    }
    else if (adder.getState() == AddKitState.PERMISSION) {
      adder.perm = (event.getMessage().equalsIgnoreCase("non") ? null : event.getMessage());

      validate(adder);
    }
  }

  private void validate(KitAdder adder) {
    KitsManager.getInstance().addKit(adder.name, adder.desc, adder.perm, adder.game, adder.item, (KitLevel[])adder.levels.toArray(new KitLevel[0]));
    adder.getPlayer().sendMessage("§6Kit créée !");

    for (int i = 0; i < this.m_adders.size(); i++)
      if (((KitAdder)this.m_adders.get(i)).getPlayer().getUniqueId().equals(adder.getPlayer().getUniqueId())) {
        this.m_adders.remove(i);
        return;
      }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event)
  {
    KitAdder adder = getKitAdder(event.getPlayer());
    if (adder == null) return;

    if (adder.getState() == AddKitState.ITEM) {
      adder.item = event.getItemDrop().getItemStack();
      adder.setState(AddKitState.LEVEL_PRICE);
      event.setCancelled(true);
    }
  }

  private KitAdder getKitAdder(Player player) {
    for (KitAdder adder : this.m_adders) {
      if (adder.getPlayer().getUniqueId().equals(player.getUniqueId())) return adder;
    }
    return null;
  }

  private static enum AddKitState
  {
    DESCRIPTION(ChatColor.GOLD + "Taper la description dans le chat"), 
    ITEM(ChatColor.DARK_GREEN + "Droppez l'item du kit"), 
    LEVEL_PRICE(ChatColor.GRAY + "Entrez le prix du niveau <level>"), 
    LEVEL_LORE(ChatColor.GRAY + "Entrez les lore du level <level> ligne par ligne (Tapper \"ok\" pour valider)"), 
    LEVEL_ITEMS(ChatColor.GRAY + "Tapper \"ok\" dans le chat pour valider l'inventaire du level <level>"), 
    PERMISSION(ChatColor.YELLOW + "Tapper \"non\" ou la permission dans le chat");

    private String m_message;

    private AddKitState(String message) { this.m_message = message; }

    public String getMessage() {
      return this.m_message;
    }
  }

  private class KitAdder
  {
    private Player m_player;
    private CommandAddKit.AddKitState m_state;
    public GameType game;
    public String name;
    public String desc;
    public String perm;
    public ItemStack item;
    public List<CommandAddKit.KitLevelAdd> levels = new LinkedList();

    public KitAdder(Player player, GameType game, String name) {
      this.m_player = player;
      this.game = game;
      this.name = name;
      setState(CommandAddKit.AddKitState.DESCRIPTION);
    }

    public void setState(CommandAddKit.AddKitState state) {
      this.m_state = state;

      int level = this.levels == null ? 0 : this.levels.size();
      if (this.m_state == CommandAddKit.AddKitState.LEVEL_PRICE) level++;

      this.m_player.sendMessage(this.m_state.getMessage().replaceAll("<level>", Integer.toString(level)));
    }
    public Player getPlayer() {
      return this.m_player; } 
    public CommandAddKit.AddKitState getState() { return this.m_state; }
  }



  public static class KitLevelAdd
  extends KitsManager.CraftKitLevel {
      public KitLevelAdd(int price) {
          super(price, new LinkedList<String>(), null);
      }

      public void setPrice(int price) {
          this.m_price = price;
      }

      public void setLore(List<String> lore) {
          this.m_lore = lore;
      }

      public void setItems(List<LinarisKitsAPI.ItemStackAndSlot> items) {
          this.m_items = items;
      }
  }


}