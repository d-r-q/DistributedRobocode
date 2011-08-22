/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.Competitor;
import ru.jdev.rc.drc.server.RobocodeServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.lang.Math.max;

/**
 * User: jdev
 * Date: 21.08.11
 */
public class ProxyManager {

    private final Object proxiesLock = new Object();
    private final List<RobocodeServerProxy> proxies;

    public ProxyManager(ExecutorService executorService, Set<Bot> bots) throws IOException {
        proxies = getProxies(executorService, bots);
        executorService.submit(new StateUpdater());
    }

    public synchronized RobocodeServerProxy getFreeProxy() {
        while (true) {

            for (RobocodeServerProxy proxy : proxies) {
                if (proxy.ready()) {
                    return proxy;
                }
            }

            synchronized (proxiesLock) {
                try {
                    proxiesLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

    public synchronized List<RobocodeServerProxy> getAvailableProxies() {
        return proxies;
    }

    private static List<RobocodeServerProxy> getProxies(final ExecutorService service, Set<Bot> bots) throws IOException {
        final List<RobocodeServerProxy> futures = new ArrayList<>();
        try (
                BufferedReader reader = new BufferedReader(new FileReader("./config/servers.cfg"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] serverCfg = line.split(";");
                final String url = String.format("http://%s:%d/RS", serverCfg[0], Integer.parseInt(serverCfg[1]));
                final Future<RobocodeServer> connectingFuture = service.submit(new RSProxyFactory(url));
                final RobocodeServerProxy proxy = new RobocodeServerProxy(serverCfg[2], url, bots);
                proxy.setConnectingFuture(connectingFuture);
                futures.add(proxy);
            }
        }

        return futures;
    }

    public List<ProxyState> getState() {
        final List<ProxyState> proxiesState = new ArrayList<>();
        for (RobocodeServerProxy proxy : proxies) {
            final BattleRequest currentBattleRequest = proxy.getCurrentBattleRequest();
            if (currentBattleRequest != null) {
                final Competitor competitor = currentBattleRequest.competitors.get(1);
                proxiesState.add(new ProxyState(proxy.getUrl(), competitor.getName() + " " + competitor.getVersion(),
                        (currentBattleRequest.state != null ? currentBattleRequest.state.getMessage() : " -- "), proxy.isConnected(), proxy.getStateMessage()));
            } else {
                proxiesState.add(new ProxyState(proxy.getUrl(), " -- ", " -- ", proxy.isConnected(), proxy.getStateMessage()));
            }
        }

        return proxiesState;
    }

    public class ProxyState {

        public final String url;
        public final String currentBot;
        public final String battleRequestState;
        public final boolean isOnline;
        public final String message;

        public ProxyState(String url, String currentBot, String battleRequestState, boolean online, String message) {
            this.url = url;
            this.currentBot = currentBot;
            this.battleRequestState = battleRequestState;
            isOnline = online;
            this.message = message;
        }
    }

    private class StateUpdater implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    long startTime = System.currentTimeMillis();
                    int maxRound = 1;
                    for (RobocodeServerProxy proxy : proxies) {
                        if (proxy.checkState()) {
                            synchronized (proxiesLock) {
                                proxiesLock.notify();
                            }
                            final BattleRequest currentBattleRequest = proxy.getCurrentBattleRequest();
                            if (currentBattleRequest != null) {
                                final String message = currentBattleRequest.state.getMessage();
                                if (message != null && message.startsWith("Round")) {
                                    maxRound = max(maxRound, Integer.parseInt(message.split(" ")[1]));
                                }
                            }
                        }
                    }

                    long timeout = startTime + (100 / max(1, maxRound / 5)) - System.currentTimeMillis();
                    if (timeout > 0) {
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

}
