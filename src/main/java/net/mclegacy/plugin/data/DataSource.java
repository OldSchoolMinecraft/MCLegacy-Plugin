package net.mclegacy.plugin.data;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface DataSource
{
    List<BanHolder> getBans(String username);
    List<ItemStack> getVirtualItems(String username);
    Shop getShop(String username);
}
