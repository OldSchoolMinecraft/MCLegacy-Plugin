package net.mclegacy.plugin.servlets;

import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.util.ResourceLoader;
import org.bukkit.Bukkit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;

public class DynmapConfig extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("dynmap"))
            {
                response.setStatus(500);
                response.getWriter().println("Dynmap not installed");
                return;
            }
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,Accept,X-Requested-With,Origin");
            response.setHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            String webPath = new File("plugins/dynmap", MCLegacy.instance.getDynmap().configuration.getString("webpath", "web/")).getAbsolutePath();
            String configPath = new File(webPath, "standalone/dynmap_config.json").getAbsolutePath();
            ResourceLoader rl = new ResourceLoader();
            rl.directTo(new FileInputStream(configPath), response.getWriter());
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(500);
        }
    }
}
