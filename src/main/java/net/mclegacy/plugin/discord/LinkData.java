package net.mclegacy.plugin.discord;

public class LinkData
{
    public String discordID;
    public String username;
    public long linkTime;

    public LinkData() {}

    public LinkData(String discordID, String username, long linkTime)
    {
        this.discordID = discordID;
        this.username = username;
        this.linkTime = linkTime;
    }
}
