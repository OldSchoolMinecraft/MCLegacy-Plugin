package net.mclegacy.plugin.commands;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.util.CommandPipe;
import net.mclegacy.plugin.util.Util;
import net.minecraft.server.BlockFire;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginPassCommands implements CommandExecutor
{
    private static final Gson gson = new Gson();
    private OkHttpClient client;

    private MCLegacy mcLegacy;
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
        this.mcLegacy = mcLegacy;
        this.client = mcLegacy.getHttpClient();
        commands.put("link", this::link);

        for (String key : commands.keySet())
            mcLegacy.getCommand(key).setExecutor(this);
    }

    private void link(CommandSender sender, String[] args)
    {
        if (!handleArgs(sender, args, 1, "&cUsage: /link <code>")) return;

        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Only players can do this!");
            return;
        }

        Bukkit.getScheduler().scheduleAsyncDelayedTask(mcLegacy, () ->
        {
            String code = args[0];

            JsonObject req = new JsonObject();
            req.addProperty("username", sender.getName());
            req.addProperty("code", code);

            Request request = new Request.Builder()
                    .url("https://mclegacy.net/api/v1/linking")
                    .addHeader("X-API-KeyHolder", MCLegacy.instance.getConfig().getString("mclegacy.holderName", "N/A"))
                    .addHeader("X-API-Key", MCLegacy.instance.getConfig().getString("mclegacy.apiKey", "N/A"))
                    .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), gson.toJson(req)))
                    .build();

            try (Response response = client.newCall(request).execute())
            {
                JsonObject res = gson.fromJson(response.body().string(), JsonObject.class);
                if (res == null)
                {
                    sender.sendMessage(ChatColor.RED + "An error occurred while linking your account.");
                    return;
                }

                if (res.get("success").getAsBoolean()) sender.sendMessage(ChatColor.GREEN + "Successfully linked your account!");
                else sender.sendMessage(ChatColor.RED + "Linking failed: " + res.get("message").getAsString());
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "An error occurred while trying to link your account.");
            }
        }, 0L);
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
