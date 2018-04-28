package ru.lanit.at.driver;

import com.google.gson.JsonObject;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.mitm.TrustSource;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.Config;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static ru.lanit.at.FrameworkConstants.DEFAULT_PROXY_CONFIG;
import static ru.lanit.at.FrameworkConstants.REMOTE_DRIVER_VARIABLE_NAME;

public class ProxyHandler {

    private Logger log = LogManager.getLogger(DriverManager.class);

    private JsonObject jsonProxy;
    private Config proxyProperties;
    private BrowserMobProxyServer server;
    private int port;


    public ProxyHandler() {
        proxyProperties = new Config(DEFAULT_PROXY_CONFIG);
        port = proxyProperties.getProperty("port", 0);
    }

    public JsonObject getJsonProxy() {
        if (jsonProxy == null) jsonProxy = startProxy();
        return jsonProxy;
    }

    private JsonObject startProxy() {
        if (proxyProperties == null || proxyProperties.isEmpty())
            throw new FrameworkRuntimeException("Proxy properties are not defined. Please initialize properties in "
                    + DEFAULT_PROXY_CONFIG + " file.");

        JsonObject jsonProxySettings = new JsonObject();

        boolean startLocal = proxyProperties.getProperty("startLocal", Boolean.FALSE);

        if (startLocal) {

            server = startLocalServer();
            if (port == 0) port = server.getPort();

            jsonProxySettings.addProperty("proxyType", "manual");

            try {
                String localHostAddress = Config.getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME) ?
                        InetAddress.getLocalHost().getHostAddress() : "127.0.0.1";
                String localSocket = localHostAddress + ":" + port;
                System.setProperty("proxyHost", localHostAddress);
                System.setProperty("proxyPort", String.valueOf(port));

                jsonProxySettings.addProperty("httpProxy", localSocket);
                jsonProxySettings.addProperty("sslProxy", localSocket);

            } catch (UnknownHostException e) {
                throw new FrameworkRuntimeException("Can't set jsonProxy host for driver.", e);
            }

        } else {
            String host = proxyProperties.getProperty("host", true);
            String socksProxy = proxyProperties.getProperty("socksProxy", false);
            String socksUsername = proxyProperties.getProperty("socksUsername", false);
            String socksPassword = proxyProperties.getProperty("socksPassword", false);

            String socket = host + ":" + port;

            jsonProxySettings.addProperty("proxyType", "manual");
            jsonProxySettings.addProperty("httpProxy", socket);
            jsonProxySettings.addProperty("sslProxy", socket);
//            jsonProxySettings.addProperty("socksProxy", socksProxy);
//            jsonProxySettings.addProperty("socksUsername", socksUsername);
//            jsonProxySettings.addProperty("socksPassword", socksPassword);
        }

        log.info("Proxy settings: {}", jsonProxySettings);
        return jsonProxySettings;
    }

    public BrowserMobProxyServer startLocalServer() {
        log.info("Starting local proxy server.");
        String domainForAutoAuthorization = proxyProperties.getProperty("domainForAutoAuthorization", false);
        String authUsername = proxyProperties.getProperty("authUsername", false);
        String authPassword = proxyProperties.getProperty("authPassword", false);
        String authType = proxyProperties.getProperty("authType", "BASIC");
        boolean trustAllServers = proxyProperties.getProperty("trustAllServers", Boolean.FALSE);

        BrowserMobProxyServer localServer = new BrowserMobProxyServer();
        if (domainForAutoAuthorization != null)
            localServer.autoAuthorization(domainForAutoAuthorization, authUsername, authPassword, AuthType.valueOf(authType.toUpperCase().trim()));
        localServer.setTrustAllServers(trustAllServers);
        localServer.start(port);
        log.info("Local proxy server started.");
        return localServer;
    }

    public void shutDownLocalServer() {
        if (server != null && server.isStarted()) {
            log.info("Starting graceful proxy shutdown.");
            server.stop();
            log.info("Proxy server gracefully shut down.");
        }
    }
}
