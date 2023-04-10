package net.mclegacy.plugin.discord;

import com.johnymuffin.discordcore.DiscordCore;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.data.AbstractDataSource;
import net.mclegacy.plugin.data.RemoteDataSource;
import net.mclegacy.plugin.websockets.auth.AuthHandlerException;
import net.mclegacy.plugin.websockets.auth.AuthPluginHandler;

import java.util.HashMap;
import java.util.Random;

public class Bot extends ListenerAdapter
{
    public static final HashMap<String, String> LINK_REQUESTS = new HashMap<>();
    private final Random random = new Random();

    private DiscordCore dCore;
    private AbstractDataSource dataSource;
    private boolean usingMySQL;

    public Bot(DiscordCore dCore, AbstractDataSource dataSource)
    {
        this.dCore = dCore;
        this.dataSource = dataSource;

        usingMySQL = (dataSource instanceof RemoteDataSource);
    }

    public void init()
    {
        dCore.getDiscordBot().jda.addEventListener(this);
    }

    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        String[] parts = event.getMessage().getContentRaw().split(" ");
        if (parts[0].equalsIgnoreCase("!link"))
        {
            if (parts.length != 2)
            {
                event.getMessage().reply("Usage: !link <minecraft username>").queue();
                return;
            }
            String username = parts[1];
            if (dataSource.isDiscordAccountLinked(username))
            {
                event.getMessage().reply("That player is already linked!").queue();
                return;
            }
            String code = generateCode();
            LINK_REQUESTS.put(code, event.getAuthor().getId());
            event.getMessage().reply("Your link code is: `" + code + "`. Login to the server, and run `/dlink " + code + "` to complete the linking process.").queue();
        }

        if (parts[0].equalsIgnoreCase("!reset"))
        {
            if (usingMySQL)
            {
                LinkData data = ((RemoteDataSource) dataSource).loadDiscordLinkDataByID(event.getAuthor().getId());
                if (data == null)
                {
                    event.getMessage().reply("You are not linked to any accounts!").queue();
                    return;
                }

                AuthPluginHandler authPluginHandler = selectAuthPlugin();
                try
                {
                    authPluginHandler.deleteAccount(data.username);
                    event.getMessage().reply("Your password has been reset successfully. Please connect to the server & re-register your account as soon as possible.").queue();
                } catch (AuthHandlerException e) {
                    event.getMessage().reply("Error: `" + e.getMessage() + "`").queue();
                }
            } else {
                if (parts.length != 2)
                {
                    event.getMessage().reply("Usage: !reset <minecraft username>").queue();
                    return;
                }
                String username = parts[1];
                if (!dataSource.isDiscordAccountLinked(username))
                {
                    event.getMessage().reply("That player is not linked!").queue();
                    return;
                }
                LinkData data = dataSource.getDiscordLinkData(username);
                if (!data.discordID.equals(event.getAuthor().getId()))
                {
                    event.getMessage().reply("You are not the owner of that account!").queue();
                    return;
                }
                AuthPluginHandler authPluginHandler = selectAuthPlugin();
                try
                {
                    authPluginHandler.deleteAccount(username);
                    event.getMessage().reply("Your password has been reset successfully. Please connect to the server & re-register your account as soon as possible.").queue();
                } catch (AuthHandlerException e) {
                    event.getMessage().reply("Error: `" + e.getMessage() + "`").queue();
                }
            }
        }
    }

    private String generateCode()
    {
        return String.format("%06d", random.nextInt(999999));
    }

    private AuthPluginHandler selectAuthPlugin()
    {
        return MCLegacy.SUPPORTED_AUTH_HANDLERS.stream()
                .filter(AuthPluginHandler::isInstalled)
                .reduce((first, second) -> {
                    throw new RuntimeException("Multiple auth plugins are installed. Please remove one of them.");
                })
                .orElseThrow(() -> new RuntimeException("No auth plugin is installed."));
    }
}
