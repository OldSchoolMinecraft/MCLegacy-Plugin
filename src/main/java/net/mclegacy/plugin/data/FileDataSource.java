package net.mclegacy.plugin.data;

import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class FileDataSource extends AbstractDataSource
{
    private static final Gson gson = new Gson();

    private File sourceDir;

    public FileDataSource(File sourceDir)
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
}
