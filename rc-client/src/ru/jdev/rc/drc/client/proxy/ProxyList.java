/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.proxy;

import ru.jdev.rc.drc.client.BattleRequestManager;
import ru.jdev.rc.drc.client.Bot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * User: jdev
 * Date: 21.08.11
 */
public class ProxyList {

    private final List<RobocodeServerProxy> proxies;

    public ProxyList(ExecutorService executorService, Set<Bot> bots, BattleRequestManager battleRequestManager) throws IOException {
        proxies = getProxies(executorService, bots, battleRequestManager);
    }

    public void connectAllProxies() {
        for (RobocodeServerProxy proxy : proxies) {
            proxy.connect();
        }
    }

    public List<RobocodeServerProxy> getAvailableProxies() {
        return proxies;
    }

    private static List<RobocodeServerProxy> getProxies(final ExecutorService service, Set<Bot> bots, BattleRequestManager battleRequestManager) throws IOException {
        final List<RobocodeServerProxy> proxies = new ArrayList<>();
        try (
                BufferedReader reader = new BufferedReader(new FileReader("./config/servers.cfg"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] serverCfg = line.split(";");
                final String url = String.format("http://%s:%d/RS", serverCfg[0], Integer.parseInt(serverCfg[1]));
                final RobocodeServerProxy proxy = new RobocodeServerProxy(serverCfg[2], url, bots, service, battleRequestManager);
                proxies.add(proxy);
            }
        }

        return proxies;
    }

}
