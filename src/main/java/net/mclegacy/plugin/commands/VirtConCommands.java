package net.mclegacy.plugin.commands;

import com.google.gson.Gson;
import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.data.AbstractDataSource;
import net.mclegacy.plugin.data.LocalDataSource;
import net.mclegacy.plugin.data.RemoteDataSource;
import net.mclegacy.plugin.util.CommandPipe;
import net.mclegacy.plugin.util.MarketUtils;
import net.mclegacy.plugin.util.Util;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class VirtConCommands implements CommandExecutor
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
        this.dataSource = mysql ? new RemoteDataSource(mcLegacy.getSQLPool()) : new LocalDataSource(new File(fileStorageDir));

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
            sender.sendMessage(ChatColor.RED + "Usage: /virtcon <add/remove/list>");
            return;
        }

        String subCommand = args[0];

        switch (subCommand)
        {
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                return;
            case "add":
                /*if (args.length < 1)
                {
                    send(sender, "&cUsage: /virtcon add");
//                    send(sender, "&cPlease note that &bALL&c will send your entire inventory to the container!");
                    return;
                }*/

                Bukkit.getScheduler().scheduleAsyncDelayedTask(mcLegacy, () ->
                {
                    try
                    {
                        ItemStack inHand = ((Player) sender).getInventory().getItemInHand();
                        if (inHand == null || inHand.getType() == null || inHand.getType().name().equalsIgnoreCase("AIR"))
                            throw new CommandException("&cYou must be holding an item to add it to your container!");
                        MarketUtils.addItemToVirtualContainer((Player) sender, inHand);
                        send(sender, "&aSuccessfully added item to container!");
                    } catch (CommandException ex) {
                        send(sender, Util.translateAlternateColorCodes('&', ex.getMessage()));
                    }
                }, 0L);

                /*if (args[1].equalsIgnoreCase("hand"))
                {
                    //TODO: implement
                } else if (args[1].equalsIgnoreCase("all")) {
                    //TODO: implement (will require some backend changes)
                }*/
                break;
            case "remove":
                try
                {
                    ItemStack inHand = ((Player) sender).getInventory().getItemInHand();
                    if (inHand == null || inHand.getType() == null || inHand.getType().name().equalsIgnoreCase("AIR"))
                        throw new CommandException("&cYou must be holding an item to add it to your container!");
                    MarketUtils.decrementStackInVirtualContainer((Player) sender, inHand);
                    send(sender, "&aSuccessfully removed item from container!");
                } catch (CommandException ex) {
                    send(sender, Util.translateAlternateColorCodes('&', ex.getMessage()));
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
