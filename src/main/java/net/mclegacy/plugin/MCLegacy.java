package net.mclegacy.plugin;

import com.google.gson.Gson;
import com.johnymuffin.discordcore.DiscordCore;
import net.mclegacy.plugin.commands.BanCommands;
import net.mclegacy.plugin.commands.LoginPassCommands;
import net.mclegacy.plugin.commands.MiscCommands;
import net.mclegacy.plugin.commands.ws.Sudo;
import net.mclegacy.plugin.commands.ws.WSCommand;
import net.mclegacy.plugin.data.AbstractDataSource;
import net.mclegacy.plugin.data.LocalDataSource;
import net.mclegacy.plugin.data.RemoteDataSource;
import net.mclegacy.plugin.discord.Bot;
import net.mclegacy.plugin.util.*;
import net.mclegacy.plugin.websockets.auth.AuthMeHandler;
import net.mclegacy.plugin.websockets.auth.AuthPluginHandler;
import net.mclegacy.plugin.websockets.auth.OSASHandler;
import net.mclegacy.plugin.websockets.auth.xAuthHandler;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCLegacy extends JavaPlugin
{
    public static final List<AuthPluginHandler> SUPPORTED_AUTH_HANDLERS = Arrays.asList(new OSASHandler(), new AuthMeHandler(), new xAuthHandler());

    private static final Gson gson = new Gson();
    public static MCLegacy instance;

    private JettyManager jettyManager;
    private PluginConfig config;
    private DynmapPlugin dynmapPlugin;
    private BanManager banManager;
    private final BanCommands banCommands = new BanCommands();
    private final LoginPassCommands loginPassCommands = new LoginPassCommands();
    private final MiscCommands miscCommands = new MiscCommands();
    private AliasMap<String, WSCommand> wsCommands = new AliasMap<>();
    private OkHttpClient httpClient;
    private MySQLConnectionPool connectionPool;
    private AbstractDataSource dataSource;
    private Bot bot;

    public void onLoad()
    {
        System.out.println("[MCLegacy] Finished loading");
    }

    public void onEnable()
    {
        try
        {
            instance = this;
            config = new PluginConfig();

            if (config.getString("plugin.dataSource.type", "mysql").equalsIgnoreCase("mysql"))
            {
                String host = config.getString("plugin.dataSource.mysql.host", "localhost");
                String port = config.getString("plugin.dataSource.mysql.port", "3306");
                String database = config.getString("plugin.dataSource.mysql.database", "mclegacy");
                String username = config.getString("plugin.dataSource.mysql.username", "mclegacy");
                String password = config.getString("plugin.dataSource.mysql.password", "mclegacy");
                connectionPool = new MySQLConnectionPool(String.format("jdbc:mysql://%s:%s/%s", host, port, database), username, password);
                dataSource = new RemoteDataSource(connectionPool);
            } else if (config.getString("plugin.dataSource.type", "mysql").equalsIgnoreCase("file")) {
                dataSource = new LocalDataSource(new File(config.getString("plugin.dataSource.file.storageDir", getDataFolder().getAbsolutePath())));
            } else {
                System.out.println("[MCLegacy] Invalid data source type");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            if (config.getBoolean("plugin.enableDiscordLinking", false))
            {
                bot = new Bot((DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore"), dataSource);
                bot.init();
            }

            wsCommands.put("sudo", new Sudo());

            getDataFolder().mkdirs();
            new File(getDataFolder(), "bans").mkdirs();
            new File(getDataFolder(), "queue").mkdirs();

            handleDependencies();

            httpClient = new OkHttpClient();

            getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
            {
                try
                {
                    jettyManager = new JettyManager();
                    jettyManager.init(this);
                } catch (Exception ex) {
                    System.out.println("[MCLegacy] Failed to start Jetty server");
                    ex.printStackTrace();
                }
            }, 5L);

            getServer().getScheduler().scheduleAsyncRepeatingTask(this, this::sendServerPing, 0, 20 * 60); // 1-minute tick timer

            //LogInterceptor interceptor = new LogInterceptor(System.out);
            //System.setOut(interceptor);
            //System.setErr(interceptor);

            //getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> banManager.tick(), 0, 20 * 5); // 5-second tick timer

            //banCommands.init(this);
            loginPassCommands.init(this);
            miscCommands.init(this);

            if (Debugger.isEnabled()) System.out.println("[MCLegacy Debugger] Enabled");
            else System.out.println("[MCLegacy] Enabled");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendServerPing()
    {
        ArrayList<String> players = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers())
            players.add(player.getName());
        String payload = gson.toJson(new ServerPing(getServer().getServerName(), getWebsocketURL(), players));

        try
        {
            try (Response response = httpClient.newCall(new okhttp3.Request.Builder()
                    .url("https://mclegacy.net/api/ping")
                    .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), payload))
                    .build()).execute()) {}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getWebsocketURL()
    {
        String protocol = "ws://";
        if (config.getBoolean("jetty.sslEnabled", false))
            protocol = "wss://";
        String host = config.getString("jetty.host", getServer().getIp());
        int port = config.getInt("plugin.jettyServerPort", 42069);
        return protocol + host + ":" + port;
    }

    public void onDisable()
    {
        try
        {
            System.out.println("[MCLegacy] Stopping Jetty server...");
            jettyManager.shutdown();
            System.out.println("[MCLegacy] Jetty stopped. Plugin disabled.");
        } catch (Exception ignored) {}
    }

    private void handleDependencies()
    {
        try
        {
            if (getServer().getPluginManager().getPlugin("dynmap") == null)
            {
                File file = new File("plugins", (String) config.getConfigOption("dependencies.dynmap", "dynmap.jar"));
                if (file.exists())
                {
                    Plugin plugin = getServer().getPluginManager().loadPlugin(file);
                    getServer().getPluginManager().enablePlugin(plugin);
                    dynmapPlugin = (DynmapPlugin) plugin;
                } else System.out.println("[MCLegacy] Missing soft dependency: " + file.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PluginConfig getConfig()
    {
        return config;
    }

    public DynmapPlugin getDynmap()
    {
        return dynmapPlugin;
    }

    public BanManager getBanManager()
    {
        return banManager;
    }

    public MySQLConnectionPool getSQLPool()
    {
        return connectionPool;
    }

    public OkHttpClient getHttpClient()
    {
        return httpClient;
    }

    public AbstractDataSource getDataSource()
    {
        return dataSource;
    }
}
