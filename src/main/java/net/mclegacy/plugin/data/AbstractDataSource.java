package net.mclegacy.plugin.data;


import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class AbstractDataSource implements DataSource
{
    protected abstract List<BanHolder> loadBans(String username);
    protected abstract List<ItemStack> loadVirtualItems(String username);
    protected abstract Shop loadShop(String username);

    public List<BanHolder> getBans(String username)
    {
        return loadBans(username);
    }

    public List<ItemStack> getVirtualItems(String username)
    {
        return loadVirtualItems(username);
    }

    public Shop getShop(String username)
    {
        return loadShop(username);
    }
}
