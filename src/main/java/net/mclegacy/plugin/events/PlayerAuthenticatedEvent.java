package net.mclegacy.plugin.events;

import net.mclegacy.plugin.websockets.auth.AuthPluginHandler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;


public class PlayerAuthenticatedEvent extends Event implements Cancellable
{
    private boolean cancelled;
    private String username, ip;
    private AuthPluginHandler authHandler;
    private String cancelReason = "N/A";

    public PlayerAuthenticatedEvent(String username, String ip, AuthPluginHandler authHandler)
    {
        super("PlayerAuthenticatedEvent");
        this.username = username;
        this.ip = ip;
        this.authHandler = authHandler;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b)
    {
        this.cancelled = b;
    }

    public void setCancelReason(String reason)
    {
        this.cancelReason = reason;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public String getUsername()
    {
        return username;
    }

    public String getIP()
    {
        return ip;
    }

    public AuthPluginHandler getAuthHandler()
    {
        return authHandler;
    }
}
