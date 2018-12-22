package net.neferett.LinarisKits.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackBuilder {
    private ItemStack m_item;

    public ItemStackBuilder(Material type) {
        this.m_item = new ItemStack(type);
    }

    public ItemStackBuilder setAmount(int amount) {
        this.m_item.setAmount(amount);
        return this;
    }

    public ItemStackBuilder setDurability(short durability) {
        this.m_item.setDurability(durability);
        return this;
    }

    public ItemStack toIS() {
        return this.m_item;
    }
}

