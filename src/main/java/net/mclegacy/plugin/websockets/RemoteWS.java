package net.mclegacy.plugin.websockets;

import net.mclegacy.plugin.MCLegacy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class RemoteWS extends CustomWebSocket
{
    public static final ArrayList<RemoteWS> sockets = new ArrayList<>();
    private final CountDownLatch closureLatch = new CountDownLatch(1);

    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        log("WebSocket connection from: " + getIP());
        if (session.getUpgradeRequest().getParameterMap().containsKey("key"))
        {
            String key = session.getUpgradeRequest().getParameterMap().get("key").get(0);
            if (!key.equals(MCLegacy.instance.getConfig().getConfigOption("plugin.remoteAccessKey")))
                closeWithMessage(session, 401, "Invalid access key");
            else {
                sockets.add(this);
                sendString("Connection successful! Online players: " + getPlayerList());
                adminBroadcast(ChatColor.GREEN + "Remote console being accessed by: " + getIP());
                long timing = 1000L;
                Timer timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    public void run()
                    {
                        ping();
                    }
                }, timing, timing);
            }
        } else closeWithMessage(session, 400, "Bad request: Access key is missing from the request");
    }



    @Override
    public void onWebSocketText(String message)
    {
        if (message.startsWith(".sudo"))
        {

            return;
        }

        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()), message);
        adminBroadcast(String.format("%s issued remote server command: %s", getIP(), message));

        super.onWebSocketText(message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        sockets.remove(this);
        log(String.format("Socket closed (%s): %s%n", statusCode, reason));
        closureLatch.countDown();
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }


}
