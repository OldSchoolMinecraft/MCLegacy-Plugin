package net.mclegacy.plugin.websockets.auth;

import com.cypherx.xauth.xAuth;
import net.mclegacy.plugin.MCLegacy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public void deleteAccount(String username) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("xAuth is not installed");
        xauth.removeAuth(username);
        Player target = MCLegacy.instance.getServer().getPlayer(username);
        if (target != null)
        {
            if (xauth.mustRegister(target))
            {
                xauth.saveLocation(target);
                xauth.saveInventory(target);
            }
        }
    }

    public boolean isInstalled()
    {
        return xauth != null;
    }
}
