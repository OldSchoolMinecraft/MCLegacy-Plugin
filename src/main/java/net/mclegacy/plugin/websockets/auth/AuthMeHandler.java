package net.mclegacy.plugin.websockets.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;

public class AuthMeHandler implements AuthPluginHandler
{
    private AuthMe authMe;

    public AuthMeHandler()
    {
        authMe = (AuthMe) Bukkit.getPluginManager().getPlugin("AuthMe");
    }

    public void authenticate(String username, String ip)
    {
        if (!isInstalled()) return;
        PlayerCache.getInstance().addPlayer(new PlayerAuth(username, "MCLegacy", ip));
    }

    public boolean isInstalled()
    {
        return authMe != null;
    }
}
