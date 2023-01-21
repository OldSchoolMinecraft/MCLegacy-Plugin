package net.mclegacy.plugin.util;

import net.mclegacy.plugin.websockets.RemoteWS;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Objects;

public class LogInterceptor extends PrintStream
{
    private PrintStream stdout;

    public LogInterceptor(PrintStream stdout) throws FileNotFoundException
    {
        super(LocalDateTime.now() + ".log");
        this.stdout = stdout;
    }

    @Override
    public void println(String str)
    {
        try
        {
            stdout.println(str);
            //if (System.getenv().containsKey("MCL_DEBUG")) return;
            RemoteWS.sockets.removeIf(Objects::isNull);
            for (RemoteWS server : RemoteWS.sockets)
                if (server != null) server.sendString(str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
