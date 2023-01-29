package net.mclegacy.plugin.websockets.auth;

public interface AuthPluginHandler
{
    void authenticate(String username, String ip) throws AuthHandlerException;
    boolean isInstalled();
}
