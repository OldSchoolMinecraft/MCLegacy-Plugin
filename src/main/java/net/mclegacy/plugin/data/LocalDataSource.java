package net.mclegacy.plugin.data;

import com.google.gson.Gson;
import net.mclegacy.plugin.discord.LinkData;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LocalDataSource extends AbstractDataSource
{
    private static final Gson gson = new Gson();

    private File sourceDir;

    public LocalDataSource(File sourceDir)
    {
        this.sourceDir = sourceDir;
    }

    @Override
    protected List<BanHolder> loadBans(String username)
    {
        throw new RuntimeException("Not implemented yet! :(");
    }

    @Override
    protected List<ItemStack> loadVirtualItems(String username)
    {
        try (FileReader reader = new FileReader(new File(sourceDir, "containers/" + username + ".json")))
        {
            return Arrays.asList(gson.fromJson(reader, ItemStack[].class));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Shop loadShop(String username)
    {
        try (FileReader reader = new FileReader(new File(sourceDir, "shops/" + username + ".json")))
        {
            return gson.fromJson(reader, Shop.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected LinkData loadDiscordLinkData(String username)
    {
        File file = new File(sourceDir, "discord/" + username + ".json");
        file.getParentFile().mkdirs();
        try (FileReader reader = new FileReader(file))
        {
            return gson.fromJson(reader, LinkData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void linkDiscordAccount(String username, String discordID)
    {
        try
        {
            File file = new File(sourceDir, "discord/" + username + ".json");
            file.getParentFile().mkdirs();
            gson.toJson(new LinkData(username, discordID, System.currentTimeMillis()), new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
