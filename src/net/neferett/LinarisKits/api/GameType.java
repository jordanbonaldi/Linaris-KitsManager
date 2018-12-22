package net.neferett.LinarisKits.api;

import net.neferett.LinarisKits.Utils.ItemStackBuilder;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GameType
{
  TOWERS("Towers", new ItemStack(Material.NETHER_FENCE)), 
  TOTEM("Totem", new ItemStack(Material.LEVER)), 
  UHC("UHC", new ItemStack(Material.GOLDEN_APPLE)), 
  FALLEN_KINGDOM("Fallen Kingdom", "FK", new ItemStack(Material.TNT)), 
  SKY_WARS("SkyWars", new ItemStack(Material.IRON_PICKAXE)), 
  RUSHS("Rushs", new ItemStack(Material.BED)), 
  PVP_SWAP("PvpSwap", new ItemStack(Material.ENDER_PEARL)), 
  SURVIVOR("Survivor", new ItemStackBuilder(Material.SKULL_ITEM).setDurability((short)2).toIS()),
  HUNGER_GAMES("HungerGames", "HG",new ItemStack(Material.DIAMOND_SWORD)),
  GLADIATOR("Gladiator", new ItemStack(Material.DIAMOND_CHESTPLATE));

  private String m_name;
  private String m_shortName;
  private ItemStack m_item;

  private GameType(String name, ItemStack item) { this(name, name, item); }

  private GameType(String name, String shortName, ItemStack item)
  {
    this.m_name = ("§r§b" + name);
    this.m_shortName = ("§r§b" + shortName);
    this.m_item = item;
    ItemMeta meta = this.m_item.getItemMeta();
    meta.setDisplayName(this.m_name);
    this.m_item.setItemMeta(meta);
  }
  public String getName() {
    return this.m_name; } 
  public String getShortName() { return this.m_shortName; } 
  public ItemStack getItem() { return this.m_item; }

  public static GameType getGameType(ItemStack is) {
    for (GameType type : values()) {
      ItemStack item = type.getItem();
      if ((item.getType().equals(is.getType())) && (item.getDurability() == is.getDurability())) return type;
    }
    return null;
  }
}