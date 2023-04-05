package net.mclegacy.plugin.data;

import net.mclegacy.plugin.discord.LinkData;
import net.mclegacy.plugin.util.MySQLConnectionPool;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RemoteDataSource extends AbstractDataSource
{
    private MySQLConnectionPool connectionPool;

    public RemoteDataSource(MySQLConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    @Override
    protected List<BanHolder> loadBans(String username)
    {
        //TODO: get bans
        throw new RuntimeException("Not implemented yet! :(");
    }

    @Override
    protected List<ItemStack> loadVirtualItems(String username)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("SELECT * FROM container_items WHERE username = ? AND amount > 0"))
        {
            stmt.setString(1, username);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            List<ItemStack> items = new ArrayList<>();
            while (rs.next())
            {
                ItemStack item = new ItemStack(rs.getInt("itemID"), rs.getInt("amount"));
                items.add(item);
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Shop loadShop(String username)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("SELECT * FROM shops WHERE username = ?"))
        {
            stmt.setString(1, username);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            if (rs.next())
                return new Shop(rs.getString("owner"), rs.getString("name"), rs.getString("description"));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected LinkData loadDiscordLinkData(String username)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("SELECT * FROM link_data WHERE username = ?"))
        {
            stmt.setString(1, username);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            if (rs.next())
                return new LinkData(rs.getString("discordID"), rs.getString("username"), rs.getLong("linkTime"));
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void linkDiscordAccount(String username, String discordID)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("INSERT INTO link_data (username, discordID, linkTime) VALUES (?, ?, ?)"))
        {
            stmt.setString(1, username);
            stmt.setString(2, discordID);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
