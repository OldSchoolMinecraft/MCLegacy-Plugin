package net.mclegacy.plugin.data;

import net.mclegacy.plugin.util.MySQLConnectionPool;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySQLDataSource extends AbstractDataSource
{
    private MySQLConnectionPool connectionPool;

    public MySQLDataSource(MySQLConnectionPool connectionPool)
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
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("SELECT * FROM container_items WHERE username = ?"))
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
}
