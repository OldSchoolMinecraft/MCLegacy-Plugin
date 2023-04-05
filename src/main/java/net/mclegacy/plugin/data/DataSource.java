package net.mclegacy.plugin.data;

import net.mclegacy.plugin.discord.LinkData;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface DataSource
{
    List<BanHolder> getBans(String username);
    List<ItemStack> getVirtualItems(String username);
    Shop getShop(String username);
    boolean isDiscordAccountLinked(String username);
    LinkData getDiscordLinkData(String username);
}
