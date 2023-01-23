package net.mclegacy.plugin.websockets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CustomWebSocket extends WebSocketAdapter
{
    protected String getIP()
    {
        return getSession().getRemoteAddress().getAddress().getHostAddress();
    }

    protected void closeWithMessage(Session session, int error, String msg)
    {
        sendString(msg);
        session.close(error, msg);
    }

    public void sendString(String str)
    {
        try
        {
            getRemote().sendString(str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void ping()
    {
        try
        {
            getRemote().sendPing(ByteBuffer.allocate(16));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void log(String msg)
    {
        System.out.println("[MCLegacy] " + msg);
    }

    protected void adminBroadcast(String msg)
    {
        String prefix = ChatColor.AQUA + "[" + ChatColor.RED + "MCLegacy" + ChatColor.AQUA + "] ";
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.hasPermission("mclegacy.ws.broadcasts") || player.isOp())
                player.sendMessage(prefix + msg);
    }

    protected String getPlayerList()
    {
        Player[] players = Bukkit.getOnlinePlayers();
        if (players.length < 1) return "None :(";
        StringBuilder sb = new StringBuilder();
        for (Player player : players)
            sb.append(player.getName()).append(", ");
        String pre = sb.toString().trim();
        return pre.substring(0, pre.length() - 1);
    }
}
