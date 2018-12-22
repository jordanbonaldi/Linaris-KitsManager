
package net.neferett.LinarisKits.api;

import net.neferett.LinarisKits.api.GameType;
import net.neferett.LinarisKits.api.KitLevel;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Kit {
    public int getId();

    public String getFieldName();

    public String getName();

    public String getDescription();

    public boolean hasPerm(Player var1);

    public GameType getGameType();

    public ItemStack getItem();

    public KitLevel getLevel(int var1);

    public void apply(Player var1);
}

