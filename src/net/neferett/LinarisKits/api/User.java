
package net.neferett.LinarisKits.api;

import java.util.UUID;

import net.neferett.LinarisKits.api.Kit;

import org.bukkit.entity.Player;

public interface User {
    public int getId();

    public Player getPlayer();

    public String getName();

    public UUID getUUID();

    public int getCoins();

    public void setCoins(int var1);

    public void addCoins(int var1);

    public void subCoins(int var1);

    public int getLevelForKit(Kit var1);

    public void addLevelForKit(Kit var1);
}

