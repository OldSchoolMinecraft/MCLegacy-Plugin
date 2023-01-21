package net.mclegacy.plugin.commands;

import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.util.CommandPipe;
import net.mclegacy.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BanCommands implements CommandExecutor
{
    private HashMap<String, CommandPipe> commands = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        CommandPipe pipe = commands.get(s);
        if (pipe == null) return false;
        pipe.execute(commandSender, args);
        return true;
    }

    public void init(MCLegacy mcLegacy)
    {
        commands.put("ban", this::ban);
        commands.put("unban", this::unban);
        commands.put("tempban", this::tempban);

        for (String key : commands.keySet())
            mcLegacy.getCommand(key).setExecutor(this);
    }

    private void ban(CommandSender sender, String[] args)
    {
        if (!handlePermissions(sender, "mclegacy.ban")) return;
        if (!handleArgs(sender, args, 1, "&cUsage: /ban <username> (reason)")) return;

        String username = args[0];
        String reason = "You have been banned!";
        String issued_by = sender.getName();

        if (args.length >= 2)
            reason = stringFromArgs(1, args);

        MCLegacy.instance.getBanManager().banPlayer(username, reason, -1, issued_by);
        broadcast("&c" + issued_by + " issued a permanent ban for " + username + ": " + reason);
    }

    private void unban(CommandSender sender, String[] args)
    {
        if (!handlePermissions(sender, "mclegacy.unban")) return;
        if (!handleArgs(sender, args, 1, "&cUsage: /unban <username>")) return;

        String username = args[0];

        if (!MCLegacy.instance.getBanManager().unbanPlayer(username))
        {
            sender.sendMessage(ChatColor.RED + "An unknown error occurred. The player could not be unbanned.");
            return;
        }
        broadcast("&c" + username + " has been unbanned by " + sender.getName());
    }

    private void tempban(CommandSender sender, String[] args)
    {
        if (!handlePermissions(sender, "mclegacy.tempban")) return;
        if (!handleArgs(sender, args, 2, "&cUsage: /tempban <username> <duration> (reason)")) return;

        try
        {
            String username = args[0];
            long duration = Util.parseDateDiff(args[1], false);
            String reason = "You have been banned!";
            String issued_by = sender.getName();

            if (args.length >= 3)
                reason = stringFromArgs(2, args);

            MCLegacy.instance.getBanManager().banPlayer(username, reason, duration, issued_by);
            broadcast("&c" + username + " has been temporarily banned by " + sender.getName() + " until: " + Util.formatUnixTime(duration));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean handlePermissions(CommandSender sender, String permission)
    {
        if (sender.hasPermission(permission) || sender.isOp()) return true;
        sender.sendMessage(ChatColor.RED + "You don't have permission to run this command!");
        return false;
    }

    private boolean handleArgs(CommandSender sender, String[] args, int required)
    {
        return handleArgs(sender, args, required, "&cInsufficient arguments");
    }

    private boolean handleArgs(CommandSender sender, String[] args, int required, String msg)
    {
        if (args.length >= required) return true;
        sender.sendMessage(Util.translateAlternateColorCodes('&', msg));
        return false;
    }

    private String stringFromArgs(int startIndex, String[] args)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++)
            sb.append(args[i] + " ");
        return sb.toString().trim();
    }

    private void broadcast(String msg)
    {
        System.out.println(Util.translateAlternateColorCodes('&', "[MCLegacy] " + msg));
        for (Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(Util.translateAlternateColorCodes('&', msg));
    }
}
