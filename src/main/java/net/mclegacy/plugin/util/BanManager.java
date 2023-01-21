package net.mclegacy.plugin.util;

import com.google.gson.Gson;
import net.mclegacy.plugin.MCLegacy;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

public class BanManager
{
    private static final Gson gson = new Gson();

    private LinkedList<BanHolder> banHolderQueue = new LinkedList<>();
    private boolean running = true;
    private boolean shouldRetry = false;
    private BanHolder lastHolder;
    private final int allowedFailedRetries = 5;
    private int iterations = 1;
    private final File queueDir = new File(MCLegacy.instance.getDataFolder(), "queue");

    private void failsafeTask()
    {
        iterations++;
        if (iterations >= allowedFailedRetries && !banHolderQueue.isEmpty())
        {
            try (FileWriter writer = new FileWriter(new File(queueDir, System.currentTimeMillis() + ".queue")))
            {
                if (!banHolderQueue.isEmpty()) gson.toJson(banHolderQueue, writer);
                banHolderQueue.clear();
                shouldRetry = false;
                lastHolder = null;
                iterations = 1;
                System.out.println("MCLegacy detected an API outage and is saving the ban queue to disk to prevent data loss.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void tick()
    {
        if (!shouldRetry && banHolderQueue.isEmpty()) return;
        BanHolder banHolder = (shouldRetry && lastHolder != null) ? lastHolder : banHolderQueue.removeLast();
        if (banHolder == null) return;
        String res = Util.postJSON(makePrivilegedAPILink("bans/issue"), gson.toJson(banHolder, BanHolder.class));
        if (res.trim().equals("OK"))
        {
            shouldRetry = false;
            lastHolder = null;
            System.out.println("Successfully posted ban to MCLegacy API: " + banHolder.username);
            return;
        }
        shouldRetry = true;
        lastHolder = banHolder;
        System.out.println(String.format("Failed to post ban to MCLegacy API (%s)! Retries left: %s", banHolder.username, allowedFailedRetries - iterations));
        failsafeTask();
    }

    public void banPlayer(String username, String reason, long expiration, String issued_by)
    {
        banHolderQueue.add(new BanHolder(username, reason, String.valueOf(expiration), issued_by, String.valueOf(System.currentTimeMillis() / 1000L)));
    }

    public boolean unbanPlayer(String username)
    {
        return Util.get(makePrivilegedAPILink("bans/unban") + "&username=" + username).trim().equals("OK");
    }

    private String makePrivilegedAPILink(String path)
    {
        PluginConfig conf = MCLegacy.instance.getConfig();
        return String.format("%s?holder=%s&apiKey=%s", Debugger.makeURL(path), conf.getConfigOption("mclegacy.holderName"), conf.getConfigOption("mclegacy.apiKey"));
    }
}
