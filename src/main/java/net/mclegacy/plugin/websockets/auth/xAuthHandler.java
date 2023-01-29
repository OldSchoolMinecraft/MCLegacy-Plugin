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

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("xAuth is not installed");
        if (!xauth.isRegistered(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        xauth.login(Bukkit.getPlayer(username));
    }

    public boolean isInstalled()
    {
        return xauth != null;
    }
}
