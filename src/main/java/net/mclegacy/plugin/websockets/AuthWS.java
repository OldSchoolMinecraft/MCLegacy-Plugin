package net.mclegacy.plugin.websockets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oldschoolminecraft.osas.OSAS;
import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.events.PlayerAuthenticatedEvent;
import net.mclegacy.plugin.util.Debugger;
import net.mclegacy.plugin.util.Util;
import net.mclegacy.plugin.websockets.auth.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AuthWS extends CustomWebSocket
{
    @Override
    public void onWebSocketText(String message)
    {
        if (!MCLegacy.instance.getConfig().getBoolean("mclegacy.enableLoginPass", true))
        {
            closeWithMessage(getSession(), 400, "Login Pass is disabled on this server!");
            return;
        }

        String[] parts = message.split(" ");

        if (parts.length != 3)
        {
            closeWithMessage(getSession(), 400, "Missing parameters");
            return;
        }

        if (!parts[0].equals("AUTH"))
        {
            closeWithMessage(getSession(), 400, "Invalid request");
            return;
        }

        String username = parts[1];
        String code = parts[2];

        Player player = Bukkit.getPlayer(username);
        if (player == null)
        {
            closeWithMessage(getSession(), 404, "Player is offline");
            return;
        }

        if (Debugger.isEnabled()) System.out.println("Received authentication request via websocket for " + username + " with code: " + code);

        Request request = new Request.Builder()
                .url("https://mclegacy.net/api/v1/login_pass?username=" + username + "&code=" + code)
                .addHeader("X-API-KeyHolder", MCLegacy.instance.getConfig().getString("mclegacy.holderName", "N/A"))
                .addHeader("X-API-Key", MCLegacy.instance.getConfig().getString("mclegacy.apiKey", "N/A"))
                .build();

        try (Response response = client.newCall(request).execute())
        {
            String rawResponse = response.body().string();
            if (Debugger.isEnabled()) System.out.println("Authentication response from MCLegacy:\n" + rawResponse);
            JsonObject resObj = new Gson().fromJson(rawResponse, JsonObject.class);

            if (resObj == null)
            {
                closeWithMessage(getSession(), 500, "Internal server error");
                return;
            }

            if ((response.code() != 200 || (!resObj.has("success") || !resObj.get("success").getAsBoolean())))
            {
                closeWithMessage(getSession(), 401, resObj.has("message") ? resObj.get("message").getAsString() : "Failed to authorize login");
                return;
            }

            if (!Objects.equals(getIP(), player.getAddress().getAddress().getHostAddress()))
            {
                closeWithMessage(getSession(), 401, "IP mismatch");
                return;
            }

            AuthPluginHandler authPlugin = selectAuthPlugin();
            try
            {
                PlayerAuthenticatedEvent event = new PlayerAuthenticatedEvent(player.getName(), player.getAddress().getAddress().getHostAddress(), authPlugin);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled())
                {
                    closeWithMessage(getSession(), 401, "Failed to authenticate: " + event.getCancelReason());
                    return;
                }
                authPlugin.authenticate(player.getName(), player.getAddress().getAddress().getHostAddress());
            } catch (AuthHandlerException ex) {
                closeWithMessage(getSession(), 401, "Failed to authenticate: " + ex.getMessage());
                return;
            }

            System.out.println("[MCLegacy] Successfully authenticated " + player.getName() + " via websocket");
            player.sendMessage(Util.translateAlternateColorCodes('&', "&aYou have been authenticated via &bMCLegacy&a!"));
            closeWithMessage(getSession(), 200, "OK");
        } catch (Exception e) {
            e.printStackTrace();
            closeWithMessage(getSession(), 500, "Internal server error: " + e.getMessage());
        }
    }

    private AuthPluginHandler selectAuthPlugin()
    {
        return MCLegacy.SUPPORTED_AUTH_HANDLERS.stream()
                .filter(AuthPluginHandler::isInstalled)
                .reduce((first, second) -> {
                    throw new RuntimeException("Multiple auth plugins are installed. Please remove one of them.");
                })
                .orElseThrow(() -> new RuntimeException("No auth plugin is installed."));
    }
}
