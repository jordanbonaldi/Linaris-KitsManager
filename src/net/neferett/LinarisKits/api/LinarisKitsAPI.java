package net.neferett.LinarisKits.api;

import java.util.List;
import java.util.Map;

import net.neferett.LinarisKits.Managers.KitsManager;
import net.neferett.LinarisKits.Managers.UsersManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class LinarisKitsAPI
{
  public static final String ADD_COINS = "§6+<coins> §bcoins";
  public static final String INV_CHOOSE_KIT = "Choisir un kit";
  public static final String KIT_CHOOSE = "§aVous avez choisi le kit §6<kit> §a!";
  public static final String ITEM_CHOOSE_KIT = "§6Choisissez un kit";
  public static final String KIT_LOCK = "§cVous n'avez pas débloquer ce kit";

  public static User getUser(Player player)
  {
    return UsersManager.getInstance().getUser(player);
  }
  public static List<Kit> getKits() { return KitsManager.getInstance().getKits(); } 
  public static List<Kit> getKitsForGame(GameType game) { return KitsManager.getInstance().getKitsForGame(game); }

  public static Kit getDefaultKit(GameType game) {
    return getKitsForGame(game).size() > 0 ? (Kit)getKitsForGame(game).get(0) : null;
  }
  public static Kit getKit(int id) {
    return KitsManager.getInstance().getKit(id); } 
  public static Kit getKit(GameType game, ItemStack is) { return KitsManager.getInstance().getKit(game, is); }


  public static String toString(ItemStack is)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(is.getType().name()).append(":").append(is.getAmount()).append(":");
    sb.append(is.getDurability()).append(":");

    ItemMeta im = is.getItemMeta();
    if (im.hasDisplayName()) sb.append(im.getDisplayName());
    sb.append(":");

    int i = 0;
    Map enchants = is.getEnchantments();
    for (Object ench : enchants.keySet()) {
      if (i > 0) sb.append(";");
      int level = ((Integer)enchants.get(ench)).intValue();
      sb.append(((Enchantment) ench).getName()).append(",").append(level);
      i++;
    }

    return sb.toString();
  }

  public static String toString(ItemStackAndSlot is_slot)
  {
    return toString(is_slot.getItemStack()) + ":" + is_slot.getSlot();
  }

  public static ItemStack toItemStack(String data)
  {
    try
    {
      String[] infos = data.split(":");
      if (infos.length < 2) return null;

      Material material = Material.getMaterial(infos[0]);
      int amount = Integer.parseInt(infos[1]);

      if (material == null) return null;

      ItemStack is = new ItemStack(material, amount);
      if ((infos.length > 2) && (!infos[2].isEmpty())) {
        is.setDurability(Short.parseShort(infos[2]));
      }

      if ((infos.length > 3) && (!infos[3].isEmpty())) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', infos[3]));
        is.setItemMeta(im);
      }

      if ((infos.length > 4) && (!infos[4].isEmpty())) {
        String echants_data = infos[4];
        String[] enchants_infos = echants_data.split(";");
        for (String enchant_infos : enchants_infos) {
          String[] enchant_infos_split = enchant_infos.split(",");
          if (enchant_infos_split.length == 2) {
            Enchantment ench = Enchantment.getByName(enchant_infos_split[0]);
            int level = Integer.parseInt(enchant_infos_split[1]);

            if (ench != null) is.addEnchantment(ench, level);
          }
        }
      }

      return is; } catch (Exception e) {
    }return null;
  }

  public static ItemStackAndSlot toItemStackAndSlot(String data)
  {
    try
    {
      int slot = Integer.parseInt(data.split(":")[5]);
      return new ItemStackAndSlot(toItemStack(data), slot); } catch (Exception e) {
    }return null;
  }
  public static class ItemStackAndSlot {
    private ItemStack m_is;
    private int m_slot;

    public ItemStackAndSlot(ItemStack is, int slot) {
      this.m_is = is;
      this.m_slot = slot;
    }
    public ItemStack getItemStack() {
      return this.m_is; } 
    public int getSlot() { return this.m_slot; }

  }
}