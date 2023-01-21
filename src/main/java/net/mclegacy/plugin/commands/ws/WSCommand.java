package net.mclegacy.plugin.commands.ws;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

public abstract class WSCommand
{
    private RemoteEndpoint socket;
    private Session session;

    public WSCommand setup(RemoteEndpoint socket, Session session)
    {
        this.socket = socket;
        this.session = session;
        return this;
    }

    public void execute(String[] args)
    {
        sendString("Command not found");
    }

    protected void sendString(String str)
    {
        try
        {
            socket.sendString(str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void adminBroadcast(String msg)
    {
        String prefix = ChatColor.AQUA + "[" + ChatColor.RED + "MCLegacy" + ChatColor.AQUA + "] ";
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.hasPermission("mclegacy.ws.broadcasts") || player.isOp())
                player.sendMessage(prefix + msg);
    }

    public String getIP()
    {
        return session.getRemoteAddress().getAddress().getHostAddress();
    }
}
