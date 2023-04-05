package net.mclegacy.plugin.data;


import net.mclegacy.plugin.discord.LinkData;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class AbstractDataSource implements DataSource
{
    protected abstract List<BanHolder> loadBans(String username);
    protected abstract List<ItemStack> loadVirtualItems(String username);
    protected abstract Shop loadShop(String username);
    protected abstract LinkData loadDiscordLinkData(String username);
    public abstract void linkDiscordAccount(String username, String discordID);

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

    public boolean isDiscordAccountLinked(String username)
    {
        return loadDiscordLinkData(username) != null;
    }

    public LinkData getDiscordLinkData(String username)
    {
        return loadDiscordLinkData(username);
    }
}
