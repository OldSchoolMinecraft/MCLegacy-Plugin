package net.mclegacy.plugin;

import com.google.gson.Gson;
import net.mclegacy.plugin.commands.BanCommands;
import net.mclegacy.plugin.commands.LoginPassCommands;
import net.mclegacy.plugin.commands.MiscCommands;
import net.mclegacy.plugin.commands.ws.Sudo;
import net.mclegacy.plugin.commands.ws.WSCommand;
import net.mclegacy.plugin.data.AbstractDataSource;
import net.mclegacy.plugin.data.FileDataSource;
import net.mclegacy.plugin.data.MySQLDataSource;
import net.mclegacy.plugin.util.*;
import okhttp3.OkHttpClient;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapPlugin;

import java.io.File;

public class MCLegacy extends JavaPlugin
{
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
                dataSource = new MySQLDataSource(connectionPool);
            } else if (config.getString("plugin.dataSource.type", "mysql").equalsIgnoreCase("file")) {
                dataSource = new FileDataSource(new File(config.getString("plugin.dataSource.file.storageDir")));
            } else {
                System.out.println("[MCLegacy] Invalid data source type");
                getServer().getPluginManager().disablePlugin(this);
                return;
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
}
