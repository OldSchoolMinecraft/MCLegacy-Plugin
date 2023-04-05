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
        generateConfigOption("plugin.enableDiscordLinking", false);

        generateConfigOption("plugin.dataSource.type", "mysql");
        generateConfigOption("plugin.dataSource.mysql.host", "localhost");
        generateConfigOption("plugin.dataSource.mysql.port", 3306);
        generateConfigOption("plugin.dataSource.mysql.database", "mclegacy");
        generateConfigOption("plugin.dataSource.mysql.username", "mclegacy");
        generateConfigOption("plugin.dataSource.mysql.password", "mclegacy");
        generateConfigOption("plugin.dataSource.file.storageDir", "plugins/MCLegacy/");
        generateConfigOption("plugin.dataSource.info", "Configure plugin data source");

        generateConfigOption("mclegacy.holderName", "CHANGE ME :D -- This will likely be your server name");
        generateConfigOption("mclegacy.apiKey", "CHANGE ME :D -- Contact MCLegacy for a key");
        generateConfigOption("mclegacy.localBansOnly", false);
        generateConfigOption("mclegacy.enableLoginPass", true);
        generateConfigOption("mclegacy.info", "Configure integration with MCLegacy servers");

        generateConfigOption("mclegacy.market.enabled", false);
        generateConfigOption("mclegacy.market.info", "Configure options for MCLegacy market");
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
