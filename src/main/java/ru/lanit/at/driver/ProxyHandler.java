package ru.lanit.at.driver;

import com.google.gson.JsonObject;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.Config;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static ru.lanit.at.FrameworkConstants.DEFAULT_PROXY_CONFIG;
import static ru.lanit.at.FrameworkConstants.PROXY_CONFIG;
import static ru.lanit.at.FrameworkConstants.REMOTE_DRIVER_VARIABLE_NAME;

public class ProxyHandler {

    private Logger log = LogManager.getLogger(ProxyHandler.class);

    private ThreadLocal<JsonObject> jsonProxy = new ThreadLocal<>();
    private Config proxyProperties;
    private ThreadLocal<BrowserMobProxyServer> server = new ThreadLocal<>();
    private ThreadLocal<Integer> port = new ThreadLocal<>();
    private String configName;
    private boolean configLoaded;


    public ProxyHandler() {
    }

    public JsonObject getJsonProxy() {
        if (jsonProxy.get() == null) jsonProxy.set(startProxy());
        return jsonProxy.get();
    }

    private JsonObject startProxy() {
        loadConfig();
        if (proxyProperties == null || proxyProperties.isEmpty())
            throw new FrameworkRuntimeException("Proxy properties are not defined. Please initialize properties in "
                    + configName + " file.");

        JsonObject jsonProxySettings = new JsonObject();

        boolean startLocal = proxyProperties.getProperty("startLocal", Boolean.FALSE);

        if (startLocal) {

            startLocalServer();
            if (port.get() == 0) port.set(server.get().getPort());

            jsonProxySettings.addProperty("proxyType", "manual");

            try {
                String localHostAddress = Config.getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME) ?
                        InetAddress.getLocalHost().getHostAddress() : "127.0.0.1";
                String localSocket = localHostAddress + ":" + port.get();
                System.setProperty("proxyHost", localHostAddress);
                System.setProperty("proxyPort", String.valueOf(port.get()));

                jsonProxySettings.addProperty("httpProxy", localSocket);
                jsonProxySettings.addProperty("sslProxy", localSocket);

            } catch (UnknownHostException e) {
                throw new FrameworkRuntimeException("Can't set jsonProxy host for driver.", e);
            }

        } else {
            String host = proxyProperties.getProperty("host", true);
            port.set(proxyProperties.getProperty("port", true));
            String socksProxy = proxyProperties.getProperty("socksProxy", false);
            String socksUsername = proxyProperties.getProperty("socksUsername", false);
            String socksPassword = proxyProperties.getProperty("socksPassword", false);

            String socket = host + ":" + port.get();

            jsonProxySettings.addProperty("proxyType", "manual");
            jsonProxySettings.addProperty("httpProxy", socket);
            jsonProxySettings.addProperty("sslProxy", socket);
        }

        log.info("Proxy settings: {}", jsonProxySettings);
        return jsonProxySettings;
    }

    public void startLocalServer() {
        log.info("Starting local proxy server.");
        String domainForAutoAuthorization = proxyProperties.getProperty("domainForAutoAuthorization", false);
        String authUsername = proxyProperties.getProperty("authUsername", false);
        String authPassword = proxyProperties.getProperty("authPassword", false);
        String authType = proxyProperties.getProperty("authType", "BASIC");
        boolean trustAllServers = proxyProperties.getProperty("trustAllServers", Boolean.FALSE);

        server.set(new BrowserMobProxyServer());
        if (domainForAutoAuthorization != null)
            server.get().autoAuthorization(domainForAutoAuthorization, authUsername, authPassword, AuthType.valueOf(authType.toUpperCase().trim()));
        server.get().setTrustAllServers(trustAllServers);
        if (port.get() == null)
            port.set(proxyProperties.getProperty("port", 0));
        server.get().start(port.get());
        log.info("Local proxy server started.");
    }

    public void shutDownLocalServer() {
        if (server.get() != null && server.get().isStarted()) {
            log.info("Starting graceful proxy shutdown.");
            server.get().stop();
            server.remove();
            port.remove();
            jsonProxy.remove();
            log.info("Proxy server gracefully shut down.");
        }
    }

    private void loadConfig() {
        if (!configLoaded) {
            configName = Config.getStringSystemProperty(PROXY_CONFIG, DEFAULT_PROXY_CONFIG);
            proxyProperties = new Config(configName);
            configLoaded = true;
        }
    }


}
