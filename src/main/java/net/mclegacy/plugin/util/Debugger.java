package net.mclegacy.plugin.util;

public class Debugger
{
    public static boolean isEnabled()
    {
        return System.getenv().containsKey("MCL_DEBUG");
    }

    public static void log(String msg)
    {
        if (!isEnabled()) return;
        System.out.println("[MCL Debugger] " + msg);
    }

    public static String makeURL(String path)
    {
        String base = isEnabled() ? "http://localhost:8080/" : "http://mclegacy.net/";
        String concat = base + path;
        return concat;
    }
}
