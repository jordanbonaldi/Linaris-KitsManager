
package net.neferett.LinarisKits.api;

import java.util.List;

import net.neferett.LinarisKits.api.LinarisKitsAPI;

public interface KitLevel {
    public int getPrice();

    public List<String> getLore();

    public List<LinarisKitsAPI.ItemStackAndSlot> getItems();
}

