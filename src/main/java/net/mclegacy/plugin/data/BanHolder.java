package net.mclegacy.plugin.data;

public class BanHolder
{
    public String banID, username, reason, expiration, issued_by, issued_at;

    public BanHolder(String banID, String username, String reason, String expiration, String issued_by, String issued_at)
    {
        this.banID = banID;
        this.username = username;
        this.reason = reason;
        this.expiration = expiration;
        this.issued_by = issued_by;
        this.issued_at = issued_at;
    }
}
