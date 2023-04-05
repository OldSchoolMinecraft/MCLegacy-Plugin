package net.mclegacy.plugin.commands;

import com.google.gson.Gson;
import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.data.AbstractDataSource;
import net.mclegacy.plugin.data.LocalDataSource;
import net.mclegacy.plugin.data.RemoteDataSource;
import net.mclegacy.plugin.util.CommandPipe;
import net.mclegacy.plugin.util.Util;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

public class ShopCommands implements CommandExecutor
{
    private static final Gson gson = new Gson();
    private OkHttpClient client;

    private MCLegacy mcLegacy;
    private HashMap<String, CommandPipe> commands = new HashMap<>();
    private AbstractDataSource dataSource;

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
        this.mcLegacy = mcLegacy;
        this.client = mcLegacy.getHttpClient();
        commands.put("shop", this::shop);
        boolean mysql = mcLegacy.getConfig().getString("plugin.dataSource.type", "mysql").equalsIgnoreCase("mysql");
        String fileStorageDir = mcLegacy.getConfig().getString("plugin.dataSource.file.storageDir", "plugins/MCLegacy/");
        this.dataSource = mysql ? new RemoteDataSource(mcLegacy.getSQLPool()) : new LocalDataSource(new File(fileStorageDir));

        for (String key : commands.keySet())
            mcLegacy.getCommand(key).setExecutor(this);
    }

    private void shop(CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command!");
            return;
        }

        if (!handlePermissions(sender, "mclegacy.shop"))
        {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command!");
            return;
        }

        if (!handleArgs(sender, args, 1))
        {
            sender.sendMessage(ChatColor.RED + "Usage: /shop <create/delete/info> [name]");
            return;
        }

        String subCommand = args[0];

        switch (subCommand)
        {
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                return;
            case "create":
                break;
            case "delete":
                //TODO: implement
                break;
            case "info":
                Bukkit.getScheduler().scheduleAsyncDelayedTask(mcLegacy, () ->
                {
                    //TODO: implement
                }, 0L);
                break;
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

    private void send(CommandSender sender, String msg)
    {
        sender.sendMessage(Util.translateAlternateColorCodes('&', msg));
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
