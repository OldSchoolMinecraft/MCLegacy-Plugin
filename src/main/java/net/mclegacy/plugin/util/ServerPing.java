package net.mclegacy.plugin.util;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ServerPing
{
    public String holderName;
    public String websocketURL;
    public ArrayList<String> players;

    public ServerPing() {}

    public ServerPing(String holderName, String websocketURL, ArrayList<String> players)
    {
        this.holderName = holderName;
        this.websocketURL = websocketURL;
        this.players = players;
    }
}
