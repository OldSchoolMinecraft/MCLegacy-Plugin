package net.mclegacy.plugin.discord;

import com.johnymuffin.discordcore.DiscordCore;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.mclegacy.plugin.data.AbstractDataSource;

import java.util.HashMap;
import java.util.Random;

public class Bot extends ListenerAdapter
{
    public static final HashMap<String, String> LINK_REQUESTS = new HashMap<>();
    private final Random random = new Random();

    private DiscordCore dCore;
    private AbstractDataSource dataSource;

    public Bot(DiscordCore dCore, AbstractDataSource dataSource)
    {
        this.dCore = dCore;
        this.dataSource = dataSource;
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
    }

    private String generateCode()
    {
        return String.format("%06d", random.nextInt(999999));
    }
}