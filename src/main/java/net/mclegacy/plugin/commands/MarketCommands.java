package net.mclegacy.plugin.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.data.AbstractDataSource;
import net.mclegacy.plugin.data.FileDataSource;
import net.mclegacy.plugin.data.MySQLDataSource;
import net.mclegacy.plugin.util.CommandPipe;
import net.mclegacy.plugin.util.Util;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MarketCommands implements CommandExecutor
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
        commands.put("virtcon", this::container);
        boolean mysql = mcLegacy.getConfig().getString("plugin.dataSource.type", "mysql").equalsIgnoreCase("mysql");
        String fileStorageDir = mcLegacy.getConfig().getString("plugin.dataSource.file.storageDir", "plugins/MCLegacy/");
        this.dataSource = mysql ? new MySQLDataSource(mcLegacy.getSQLPool()) : new FileDataSource(new File(fileStorageDir));

        for (String key : commands.keySet())
            mcLegacy.getCommand(key).setExecutor(this);
    }

    private void container(CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command!");
            return;
        }

        if (!handlePermissions(sender, "mclegacy.virtcon"))
        {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command!");
            return;
        }

        if (!handleArgs(sender, args, 1))
        {
            sender.sendMessage(ChatColor.RED + "Usage: /virtcon <add/list>"); // removing handled on the website
            return;
        }

        String subCommand = args[0];

        switch (subCommand)
        {
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                return;
            case "add":
                if (args.length < 2)
                {
                    send(sender, "&cUsage: /virtcon add <hand/all>");
                    send(sender, "&cPlease note that &bALL&c will send your entire inventory to the container!");
                    return;
                }

                if (args[1].equalsIgnoreCase("hand"))
                {
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(mcLegacy, () ->
                    {
                        try
                        {
                            addItemToVirtualContainer((Player) sender, ((Player) sender).getInventory().getItemInHand());
                            send(sender, "&aSuccessfully added item to container!");
                        } catch (CommandException ex) {
                            send(sender, Util.translateAlternateColorCodes('&', ex.getMessage()));
                        }
                    }, 0L);
                } else if (args[1].equalsIgnoreCase("all")) {
                    //TODO: implement (will require some backend changes)
                }
                break;
            case "list":
                Bukkit.getScheduler().scheduleAsyncDelayedTask(mcLegacy, () ->
                {
                    try
                    {
                        List<ItemStack> items = dataSource.getVirtualItems(((Player) sender).getName());
                        if (items == null) throw new CommandException("&cAn error occurred while fetching your container items!");
                        if (items.isEmpty()) throw new CommandException("&cYour container is empty!");
                        send(sender, "&aItems in your container:");
                        for (ItemStack itemStack : items)
                            send(sender, "&b" + itemStack.getAmount() + "x &f" + itemStack.getType().name());
                    } catch (CommandException ex) {
                        send(sender, Util.translateAlternateColorCodes('&', ex.getMessage()));
                    }
                }, 0L);
                break;
        }
    }

    private void addItemToVirtualContainer(Player player, ItemStack itemStack) throws CommandException
    {
        try (PreparedStatement stmt = mcLegacy.getSQLPool().getConnection().prepareStatement("INSERT INTO container_items (username, itemID, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + ?"))
        {
            stmt.setString(1, player.getName());
            stmt.setInt(2, itemStack.getTypeId());
            stmt.setInt(3, itemStack.getAmount());
            stmt.setInt(4, itemStack.getAmount());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException("&cAn error occurred while adding item(s) to your container!");
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
