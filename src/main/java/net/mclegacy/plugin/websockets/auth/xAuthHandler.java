package net.mclegacy.plugin.websockets.auth;

import com.cypherx.xauth.xAuth;
import org.bukkit.Bukkit;

public class xAuthHandler implements AuthPluginHandler
{
    private xAuth xauth;

    public xAuthHandler()
    {
        xauth = (xAuth) Bukkit.getPluginManager().getPlugin("xAuth");
    }

    public void authenticate(String username, String ip)
    {
        if (!isInstalled()) return;
        xauth.login(Bukkit.getPlayer(username));
    }

    public boolean isInstalled()
    {
        return xauth != null;
    }
}
