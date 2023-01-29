package net.mclegacy.plugin.websockets.auth;

import com.oldschoolminecraft.osas.OSAS;
import org.bukkit.Bukkit;

public class OSASHandler implements AuthPluginHandler
{
    private OSAS osas;

    public OSASHandler()
    {
        osas = (OSAS) Bukkit.getPluginManager().getPlugin("OSAS");
    }

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("OSAS is not installed");
        if (!osas.fallbackManager.isRegistered(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        osas.fallbackManager.authenticatePlayer(username);
        osas.fallbackManager.unfreezePlayer(username);
    }

    public boolean isInstalled()
    {
        return osas != null;
    }
}
