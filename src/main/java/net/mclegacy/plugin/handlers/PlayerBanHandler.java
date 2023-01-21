package net.mclegacy.plugin.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPreLoginEvent;

import java.io.File;

public class PlayerBanHandler extends PlayerListener
{
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event)
    {
        File banFile = getPlayerBanFile(event.getName());

        if (!banFile.exists())
        {
            //
        }
    }

    private File getPlayerBanFile(String username)
    {
        return new File("plugins/MCLegacy/bans", username.toLowerCase().charAt(0) + ".json");
    }
}
