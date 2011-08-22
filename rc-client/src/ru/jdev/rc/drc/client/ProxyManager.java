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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: jdev
 * Date: 21.08.11
 */
public class ProxyManager {

    private final List<RobocodeServerProxy> availableProxies = new ArrayList<>();

    private final ExecutorService executorService;
    private final List<Future<RobocodeServerProxy>> pendingProxies;
    private final Set<Bot> bots;

    public ProxyManager(ExecutorService executorService, Set<Bot> bots) throws IOException {
        this.executorService = executorService;
        this.bots = bots;

        pendingProxies = getProxiesFutures(executorService);
    }

    public synchronized RobocodeServerProxy getFreeProxy() {
        while (true) {
            checkPendingProxies();

            for (RobocodeServerProxy proxy : availableProxies) {
                if (proxy.ready()) {
                    return proxy;
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public synchronized List<RobocodeServerProxy> getAvailableProxies() {
        return availableProxies;
    }

    private void checkPendingProxies() {
        for (Iterator<Future<RobocodeServerProxy>> iter = pendingProxies.iterator(); iter.hasNext(); ) {
            final Future<RobocodeServerProxy> future = iter.next();
            try {
                if (future.isDone()) {
                    final RobocodeServerProxy robocodeServerProxy = future.get();
                    if (robocodeServerProxy != null) {
                        loadCompetitors(robocodeServerProxy.getServerPort());
                        availableProxies.add(robocodeServerProxy);
                        iter.remove();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (pendingProxies.size() == 0 && availableProxies.size() == 0) {
            throw new IllegalStateException("No available servers");
        }
    }

    private void loadCompetitors(final RobocodeServer serverPort) {
        final List<Competitor> requiredCompetitors = new ArrayList<>();
        final Map<String, byte[]> competitorsCode = new HashMap<>();
        for (Bot bot : bots) {
            requiredCompetitors.add(bot.getCompetitor());
            competitorsCode.put(bot.getBotName() + bot.getBotVersion(), bot.getCode());
        }

        final List<Competitor> missedCompetitors = serverPort.getMissedCompetitors(requiredCompetitors);
        for (Competitor competitor : missedCompetitors) {
            registerCompetitor(competitor, serverPort, competitorsCode.get(competitor.getName() + competitor.getVersion()));
        }
    }

    private void registerCompetitor(Competitor competitor, RobocodeServer serverPort, byte[] code) {
        if (!serverPort.hasCompetitor(competitor)) {
            System.out.printf("Registering competitor %s %s\n", competitor.getName(), competitor.getVersion());
            serverPort.registerCode(competitor, code);
        }
    }

    private static List<Future<RobocodeServerProxy>> getProxiesFutures(final ExecutorService service) throws IOException {
        final List<Future<RobocodeServerProxy>> futures = new ArrayList<>();
        try (
                BufferedReader reader = new BufferedReader(new FileReader("./config/servers.cfg"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] serverCfg = line.split(";");
                futures.add(service.submit(new RSProxyFactory(serverCfg[2], serverCfg[0], Integer.parseInt(serverCfg[1]))));
            }
        }

        return futures;
    }

    public List<ProxyState> getState() {
        final List<ProxyState> proxiesState = new ArrayList<>();
        for (RobocodeServerProxy proxy : availableProxies) {
            final BattleRequest currentBattleRequest = proxy.getCurrentBattleRequest();
            if (currentBattleRequest != null) {
                final Competitor competitor = currentBattleRequest.competitors.get(1);
                proxiesState.add(new ProxyState(proxy.getUrl(), competitor.getName() + " " + competitor.getVersion(),
                        currentBattleRequest.state.getMessage(), true));
            }
        }
        System.out.println("APS: " + availableProxies.size());
        System.out.println("PSS: " + proxiesState.size());


        return proxiesState;
    }

    public class ProxyState {

        public final String url;
        public final String currentBot;
        public final String battleRequestState;
        public boolean isOnline;

        public ProxyState(String url, String currentBot, String battleRequestState, boolean online) {
            this.url = url;
            this.currentBot = currentBot;
            this.battleRequestState = battleRequestState;
            isOnline = online;
        }
    }

}
