package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.RobocodeServerService;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class RSProxyFactory implements Callable<RobocodeServerProxy> {

    private final String authToken;
    private final String host;
    private final int port;

    public RSProxyFactory(String authToken, String host, int port) {
        this.authToken = authToken;
        this.host = host;
        this.port = port;
    }

    @Override
    public RobocodeServerProxy call() throws Exception {
        final String url = String.format("http://%s:%d/RS", host, port);
        try {
            System.out.printf("Connecting to %s\n", url);
            RobocodeServerService robocodeServerService = new RobocodeServerService(new URL(url));
            final RobocodeServerProxy proxy = new RobocodeServerProxy(robocodeServerService.getRobocodeServerPort(), authToken, url);
            System.out.printf("Connected to %s\n", url);
            return proxy;
        } catch (Throwable t) {
            System.out.printf("Connect to %s failed: %s\n", url, t.getMessage());
            return null;
        }
    }
}
