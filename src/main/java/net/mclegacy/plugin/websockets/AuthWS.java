package net.mclegacy.plugin.websockets;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.oldschoolminecraft.osas.OSAS;
import net.mclegacy.plugin.util.Jwt;
import net.mclegacy.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;

public class AuthWS extends CustomWebSocket
{
    @Override
    public void onWebSocketText(String message)
    {
        if (getSession().getUpgradeRequest().getHeaders().containsKey("Authorization"))
        {
            String accessToken = getSession().getUpgradeRequest().getHeaders().get("Authorization").get(0).split(" ")[2]; // 0 = Authorization, 1 = Bearer, 2 = token
            DecodedJWT jwt = Jwt.decodeJWT(accessToken);

            if (jwt == null)
            {
                closeWithMessage(getSession(), 401, "Invalid access token");
                return;
            }

            String username = jwt.getClaim("username").asString();
            Player player = Bukkit.getPlayer("username");

            if (player == null)
            {
                closeWithMessage(getSession(), 404, "Player is offline");
                return;
            }

            if (hasAuthMe() && hasOSAS())
                throw new RuntimeException("Both AuthMe and OSAS are installed. Please remove one of them.");
            if (hasAuthMe()) PlayerCache.getInstance().addPlayer(new PlayerAuth(username, "MCLegacy", getIP()));
            else if (hasOSAS()) {
                OSAS.instance.fallbackManager.authenticatePlayer(username);
                OSAS.instance.fallbackManager.unfreezePlayer(username);
                player.sendMessage(Util.translateAlternateColorCodes('&', "&aYou have been authenticated via &bMCLegacy&a!"));
            }
        } else sendString("Bad request: Access key is missing from the request");
    }

    private boolean hasAuthMe()
    {
        return Bukkit.getPluginManager().getPlugin("AuthMe") != null;
    }

    private boolean hasOSAS()
    {
        return Bukkit.getPluginManager().getPlugin("OSAS") != null;
    }
}
