package net.mclegacy.plugin.util;

import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.commands.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;

public class MarketUtils
{
    private static MCLegacy mcLegacy = MCLegacy.instance;

    public static void addItemToVirtualContainer(Player player, ItemStack itemStack) throws CommandException
    {
        try (PreparedStatement stmt = mcLegacy.getSQLPool().getConnection().prepareStatement("INSERT INTO container_items (username, itemID, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + ?"))
        {
            stmt.setString(1, player.getName());
            stmt.setInt(2, itemStack.getTypeId());
            stmt.setInt(3, itemStack.getAmount());
            stmt.setInt(4, itemStack.getAmount());
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException("&cAn error occurred while adding item(s) to your container!");
        }
    }

    public static void decrementStackInVirtualContainer(Player player, ItemStack itemStack) throws CommandException
    {
        try (PreparedStatement stmt = mcLegacy.getSQLPool().getConnection().prepareStatement("UPDATE container_items SET amount = amount - ? WHERE username = ? AND itemID = ?"))
        {
            stmt.setInt(1, itemStack.getAmount());
            stmt.setString(2, player.getName());
            stmt.setInt(3, itemStack.getTypeId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException("&cAn error occurred while removing item(s) from your container!");
        }
    }
}
