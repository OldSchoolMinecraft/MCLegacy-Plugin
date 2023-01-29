package net.mclegacy.plugin.util;

import net.mclegacy.plugin.MCLegacy;
import net.mclegacy.plugin.util.Util;
import org.bukkit.util.config.Configuration;

import java.io.File;

public class PluginConfig extends Configuration
{
    public PluginConfig()
    {
        super(new File(MCLegacy.instance.getDataFolder(), "config.yml"));
        this.reload();
    }

    public void reload()
    {
        this.load();
        this.write();
        this.save();
    }

    private void write()
    {
        generateConfigOption("plugin.remoteAccessKey", Util.randomString(24));
        generateConfigOption("plugin.jettyServerPort", 42069);
        generateConfigOption("plugin.sslEnabled", false);
        generateConfigOption("plugin.keystorePassword", Util.randomString(32));
        generateConfigOption("plugin.sslDomain", "change-me.example.com");
        generateConfigOption("plugin.info", "Configure plugin behavior");

        generateConfigOption("mclegacy.holderName", "CHANGE ME :D -- This will likely be your server name");
        generateConfigOption("mclegacy.apiKey", "CHANGE ME :D -- Contact MCLegacy for a key");
        generateConfigOption("mclegacy.localBansOnly", false);
        generateConfigOption("mclegacy.enableLoginPass", true);
        generateConfigOption("mclegacy.info", "Configure integration with MCLegacy servers");
    }

    private void generateConfigOption(String key, Object defaultValue)
    {
        if (this.getProperty(key) == null) this.setProperty(key, defaultValue);
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    public Object getConfigOption(String key)
    {
        return this.getProperty(key);
    }

    public Object getConfigOption(String key, Object defaultValue)
    {
        Object value = getConfigOption(key);
        if (value == null) value = defaultValue;
        return value;
    }
}
