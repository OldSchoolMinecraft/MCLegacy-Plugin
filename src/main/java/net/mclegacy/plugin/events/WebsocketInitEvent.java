package net.mclegacy.plugin.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebsocketInitEvent extends Event implements Cancellable
{
    private ServletContextHandler handler;
    private boolean cancelled;

    public WebsocketInitEvent(ServletContextHandler handler)
    {
        super("WebsocketInitEvent");
        this.handler = handler;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b)
    {
        this.cancelled = b;
    }

    public void registerServlet(String pathSpec, WebSocketCreator creator)
    {

    }

    private WebSocketServlet createWebSocketServlet(WebSocketCreator creator)
    {
        return new WebSocketServlet()
        {
            public void configure(WebSocketServletFactory factory)
            {
                factory.setCreator(creator);
            }
        };
    }
}
