package net.mclegacy.plugin.commands.ws;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Sudo extends WSCommand
{
    public void execute(String[] args)
    {
        String username = args[1];
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) sb.append(args[i] + " ");
        String command = sb.toString().trim();
        Player player = Bukkit.getPlayer(username);
        if (!player.isOnline())
        {
            sendString("You can't use sudo on offline players!");
            return;
        }
        Bukkit.getServer().dispatchCommand(player, command);
        adminBroadcast(String.format("%s issued sudo command for %s: %s", getIP(), player.getName(), command));
    }
}
