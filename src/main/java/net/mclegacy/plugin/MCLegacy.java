package net.mclegacy.plugin;

import com.google.gson.Gson;
import net.mclegacy.plugin.commands.BanCommands;
import net.mclegacy.plugin.commands.ws.Sudo;
import net.mclegacy.plugin.commands.ws.WSCommand;
import net.mclegacy.plugin.servlets.DynmapConfig;
import net.mclegacy.plugin.servlets.DynmapWorld;
import net.mclegacy.plugin.util.*;
import net.mclegacy.plugin.websockets.RemoteWS;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapPlugin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.io.File;

public class MCLegacy extends JavaPlugin
{
    private static final Gson gson = new Gson();
    public static MCLegacy instance;
    private Server server;
    private ServerConnector connector;
    private PluginConfig config;
    private DynmapPlugin dynmapPlugin;
    private BanManager banManager;
    private final BanCommands banCommands = new BanCommands();
    private AliasMap<String, WSCommand> wsCommands = new AliasMap<>();

    public void onLoad()
    {
        instance = this;
        server = new Server();
        banManager = new BanManager();
        connector = new ServerConnector(server);
        config = new PluginConfig();
        connector.setPort((Integer) config.getConfigOption("plugin.jettyServerPort", 42069));
        server.addConnector(connector);

        wsCommands.put("sudo", new Sudo());

        getDataFolder().mkdirs();
        new File(getDataFolder(), "bans").mkdirs();
        new File(getDataFolder(), "queue").mkdirs();

        handleDependencies();

        System.out.println("[MCLegacy] Finished loading");
    }

    public void onEnable()
    {
        try
        {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();

            ServletContextHandler handler = new ServletContextHandler();

            if (dynmapPlugin != null)
            {
                String tilesPath = new File("plugins/dynmap/", dynmapPlugin.configuration.getString("tilespath", "web/tiles/")).getAbsolutePath();
                ServletHolder dynmapHolder = new ServletHolder("dynmap", DefaultServlet.class);
                dynmapHolder.setInitParameter("resourceBase", tilesPath);
                dynmapHolder.setInitParameter("dirAllowed", "true");
                dynmapHolder.setInitParameter("pathInfoOnly", "true");
                handler.addServlet(dynmapHolder, "/dynmap_tiles/*");

                handler.addServlet(DynmapConfig.class, "/dmap_config");
                handler.addServlet(DynmapWorld.class, "/dmap_world");

                System.out.println("MCLegacy has detected that Dynmap is installed.");
                System.out.println("MCLegacy will serve Dynmap tiles from: " + tilesPath);
            }

            handler.addServlet(new ServletHolder(new WebSocketServlet()
            {
                public void configure(WebSocketServletFactory factory)
                {
                    factory.setCreator(((servletUpgradeRequest, servletUpgradeResponse) -> new RemoteWS()));
                }
            }), "/remote");

            LogInterceptor interceptor = new LogInterceptor(System.out);
            System.setOut(interceptor);
            System.setErr(interceptor);

            getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> banManager.tick(), 0, 20 * 5); // 5-second tick timer

            banCommands.init(this);

            server.setHandler(handler);
            server.start();

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
            server.stop();
            System.out.println("[MCLegacy] Jetty stopped. Plugin disabled.");
        } catch (Exception ignored) {}
    }

    private void handleDependencies()
    {
        try
        {
            if (getServer().getPluginManager().getPlugin("dynmap") == null)
            {
                File file = new File("plugins", (String) config.getConfigOption("dependencies.dynmapFileName", "DynmapRedux.jar"));
                if (file.exists())
                {
                    Plugin plugin = getServer().getPluginManager().loadPlugin(file);
                    getServer().getPluginManager().enablePlugin(plugin);
                    dynmapPlugin = (DynmapPlugin) plugin;
                } else System.out.println("[MCLegacy] Missing soft dependency: " + file.getName());
            }

            if (getServer().getPluginManager().getPlugin("JettyLib") == null)
            {
                File file = new File("plugins", (String) config.getConfigOption("dependencies.jettylibFileName", "JettyLib-all.jar"));
                if (!file.exists())
                {
                    System.out.println("[MCLegacy] Missing required dependency: " + file.getName());
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                Plugin plugin = getServer().getPluginManager().loadPlugin(file);
                getServer().getPluginManager().enablePlugin(plugin);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DynmapPlugin getDynmapPlugin()
    {
        DynmapPlugin dynmapPlugin = (DynmapPlugin) getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin != null) return dynmapPlugin;
        try
        {
            //TODO: This is a hacky way to get the DynmapPlugin instance. Find a better way and/or make dynmapFile a config option
            String dynmapFile = System.getenv().containsKey("dynmapFile") ? System.getenv("dynmapFile") : "plugins/DynmapRedux.jar";
            dynmapPlugin = (DynmapPlugin) getServer().getPluginManager().loadPlugin(new File(dynmapFile));
            return dynmapPlugin;
        } catch (Exception ignored) { return null; }
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
}
