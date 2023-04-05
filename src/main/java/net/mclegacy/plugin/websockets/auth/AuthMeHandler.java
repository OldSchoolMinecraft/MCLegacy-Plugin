package net.mclegacy.plugin.websockets.auth;

import org.bukkit.Bukkit;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;

import java.lang.reflect.Field;

public class AuthMeHandler implements AuthPluginHandler
{
    private AuthMe authMe;
    private DataSource database;

    public AuthMeHandler()
    {
        authMe = (AuthMe) Bukkit.getPluginManager().getPlugin("AuthMe");
        try
        {
            Field field = authMe.getClass().getDeclaredField("database");
            field.setAccessible(true);
            database = (DataSource) field.get(authMe);
        } catch (Exception ex) {
            System.out.println("MCLegacy failed to access AuthMe database");
            ex.printStackTrace();
        }
    }

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
        if (!database.isAuthAvailable(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        PlayerCache.getInstance().addPlayer(new PlayerAuth(username, "MCLegacy", ip));
    }

    public void deleteAccount(String username) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
        PlayerCache.getInstance().removePlayer(username);
        database.removeAuth(username);
    }

    public boolean isInstalled()
    {
        return authMe != null;
    }
}
