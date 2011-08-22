/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.RobocodeServer;
import ru.jdev.rc.drc.server.RobocodeServerService;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class RSProxyFactory implements Callable<RobocodeServer> {

    private final String url;

    public RSProxyFactory(String url) {
        this.url = url;
    }

    @Override
    public RobocodeServer call() throws Exception {
        try {
            System.out.printf("Connecting to %s\n", url);
            RobocodeServerService robocodeServerService = new RobocodeServerService(new URL(url));
            return robocodeServerService.getRobocodeServerPort();
        } catch (Throwable t) {
            System.out.printf("Connect to %s failed: %s\n", url, t.getMessage());
            return null;
        }
    }
}
