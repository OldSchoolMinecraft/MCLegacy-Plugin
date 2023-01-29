package net.mclegacy.plugin;

import com.oldschoolminecraft.osas.impl.fallback.Account;
import net.mclegacy.plugin.events.WebsocketInitEvent;
import net.mclegacy.plugin.util.Util;
import net.mclegacy.plugin.websockets.AuthWS;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.junit.jupiter.api.Order;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class JettyManager
{
    private Server server;
    private ServerConnector insecureConnector;

    public void init(MCLegacy mcLegacy) throws Exception
    {
        try
        {
            Class.forName("org.eclipse.jetty.server.Server");
        } catch (Exception ex) {
            mcLegacy.getServer().getPluginManager().disablePlugin(mcLegacy);
            throw new RuntimeException("Jetty not loaded properly");
        }

        server = new Server();
        server.setStopAtShutdown(true);

        HttpConfiguration httpConfig = new HttpConfiguration(); // instantiate http config
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

        httpConfig.setSendServerVersion(false);

        insecureConnector = new ServerConnector(server);
        insecureConnector.setPort((Integer) mcLegacy.getConfig().getConfigOption("plugin.jettyServerPort", 42069));

        handleCertificates(mcLegacy);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStoreType("PKCS12");
        sslContextFactory.setKeyStorePath("plugins/MCLegacy/keystore.p12");
        sslContextFactory.setKeyStorePassword(mcLegacy.getConfig().getString("plugin.keystorePassword", Util.generateString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 32)));

        SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());
        ServerConnector sslConnector = new ServerConnector(server, tls, http11);
        sslConnector.setPort(mcLegacy.getConfig().getInt("plugin.jettyServerPort", 69420));

        if (mcLegacy.getConfig().getBoolean("plugin.sslEnabled", false) && new File("plugins/MCLegacy/keystore.p12").exists())
            server.addConnector(sslConnector);
        else server.addConnector(insecureConnector);

        ServletContextHandler handler = new ServletContextHandler();

        /*if (dynmapPlugin != null)
        {
            String tilesPath = new File("plugins/dynmap/", dynmapPlugin.configuration.getString("tilespath", "web/tiles/")).getAbsolutePath();
            ServletHolder dynmapHolder = new ServletHolder("dynmap", DefaultServlet.class);
            dynmapHolder.setInitParameter("resourceBase", tilesPath);
            dynmapHolder.setInitParameter("dirAllowed", "true");
            dynmapHolder.setInitParameter("pathInfoOnly", "true");
            handler.addServlet(dynmapHolder, "/dynmap_tiles/*");

            handler.addServlet(DynmapConfig.class, "/dmap_config");
            handler.addServlet(DynmapWorld.class, "/dmap_world");

            System.out.println("MCLegacy has detected that Dynmap is installed.");
            System.out.println("MCLegacy will serve Dynmap tiles from: " + tilesPath);
        }*/

        WebsocketInitEvent event = new WebsocketInitEvent(handler);
        mcLegacy.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) // TODO: Add a config option to disable this
            handler.addServlet(new ServletHolder(createWebSocketServlet((servletUpgradeRequest, servletUpgradeResponse) -> new AuthWS())), "/auth");
        //handler.addServlet(new ServletHolder(createWebSocketServlet((servletUpgradeRequest, servletUpgradeResponse) -> new RemoteWS())), "/remote");

        server.setHandler(handler);
        server.start();
    }

    private void handleCertificates(MCLegacy mcLegacy) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException
    {
        if (!mcLegacy.getConfig().getBoolean("plugin.sslEnabled", false)) return;
        String domain = mcLegacy.getConfig().getString("plugin.sslDomain", "change-me.example.com");
        File certPem = new File("/etc/letsencrypt/live/" + domain + "/cert.pem");
        File chainPem = new File("/etc/letsencrypt/live/" + domain + "/chain.pem");
        File fullchainPem = new File("/etc/letsencrypt/live/" + domain + "/fullchain.pem");
        File privkeyPem = new File("/etc/letsencrypt/live/" + domain + "/privkey.pem");
        File keystoreFile = new File("plugins/MCLegacy/keystore.p12");

        if (!certPem.exists()) return;
        if (!chainPem.exists()) return;
        if (!fullchainPem.exists()) return;
        if (!privkeyPem.exists()) return;
        if (keystoreFile.exists()) return;

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(null, mcLegacy.getConfig().getString("plugin.keystorePassword", Util.randomString(32)).toCharArray());


        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(Files.newInputStream(certPem.toPath()));
        keystore.setCertificateEntry("cert", cert);

        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Files.readAllBytes(privkeyPem.toPath()));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(privKeySpec);
        keystore.setKeyEntry("privkey", privateKey, mcLegacy.getConfig().getString("plugin.keystorePassword", Util.randomString(32)).toCharArray(), new Certificate[]{cert});
    }

    public void shutdown() throws Exception
    {
        server.stop();
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
