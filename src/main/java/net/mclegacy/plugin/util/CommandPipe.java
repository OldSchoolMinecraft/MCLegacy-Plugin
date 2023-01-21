package net.mclegacy.plugin.util;

import org.bukkit.command.CommandSender;

public interface CommandPipe
{
    void execute(CommandSender sender, String[] args);
}
