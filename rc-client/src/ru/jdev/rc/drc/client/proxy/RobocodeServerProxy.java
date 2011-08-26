/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.proxy;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.BattleRequestManager;
import ru.jdev.rc.drc.client.Bot;
import ru.jdev.rc.drc.server.*;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class RobocodeServerProxy implements Runnable {

    private final Set<ProxyListener> listeners = new HashSet<>();
    private final List<BattleRequest> enqueuedRequests = new ArrayList<>();

    private final String authToken;
    private final String url;
    private final Set<Bot> bots;
    private final ExecutorService executorService;
    private final BattleRequestManager battleRequestManager;

    private RobocodeServer serverPort;
    private String stateMessage;
    private boolean connected;
    private volatile boolean runned;

    public RobocodeServerProxy(String authToken, String url, Set<Bot> bots, ExecutorService executorService,
                               BattleRequestManager battleRequestManager) {
        this.authToken = authToken;
        this.url = url;
        this.bots = bots;
        this.executorService = executorService;
        this.battleRequestManager = battleRequestManager;
    }

    public synchronized void connect() {
        if (runned) {
            return;
        }
        executorService.submit(this);
    }

    public String getUrl() {
        return url;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public boolean isConnected() {
        return connected;
    }

    public void addListener(ProxyListener listener) {
        listeners.add(listener);
    }

    @Override
    public void run() {
        runned = true;
        try {
            stateMessage = String.format("Connecting to %s\n", url);
            notifyListeners();
            RobocodeServerService robocodeServerService = new RobocodeServerService(new URL(url));
            serverPort = robocodeServerService.getRobocodeServerPort();
            connected = true;
            stateMessage = "Loading competitors";
            notifyListeners();
            loadCompetitors();
            stateMessage = "Connected";
            notifyListeners();

            while (!Thread.currentThread().isInterrupted() && (battleRequestManager.hasPendingRequests() || enqueuedRequests.size() > 0)) {
                while (enqueuedRequests.size() < 2 && battleRequestManager.hasPendingRequests()) {
                    final BattleRequest battleRequest = battleRequestManager.getBattleRequest();
                    if (battleRequest != null) {
                        enqueueBattle(battleRequest);
                    }
                }

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Proxy interrupted");
                        // reset interrupted status
                        Thread.currentThread().interrupt();
                        break;
                    }
                    checkRequestsState();
                    final BattleRequest currentRequest = getCurrentRequest();
                    if (currentRequest.state.getState() == State.EXECUTED) {
                        battleRequestManager.battleRequestExecuted(currentRequest);
                        enqueuedRequests.remove(0);
                        break;
                    } else if (currentRequest.state.getState() == State.REJECTED) {
                        battleRequestManager.battleRequestRejected(currentRequest);
                        enqueuedRequests.remove(0);
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            stateMessage = "Server is unavailable";
        }

        if (enqueuedRequests.size() > 0) {
            for (int i = enqueuedRequests.size() - 1; i >= 0; i--) {
                try {
                    final BattleRequest request = enqueuedRequests.get(i);
                    System.out.printf("Cancel battle request %d\n", request.remoteId);
                    serverPort.cancelRequest(request.remoteId);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        connected = false;
        runned = false;
        if (!Thread.currentThread().isInterrupted()) {
            notifyListeners();
        }
        System.out.println("Proxy handling thread finished");
    }

    private void loadCompetitors() {
        final List<Competitor> requiredCompetitors = new ArrayList<>();
        final Map<String, byte[]> competitorsCode = new HashMap<>();
        for (Bot bot : bots) {
            requiredCompetitors.add(bot.getCompetitor());
            competitorsCode.put(bot.getBotName() + bot.getBotVersion(), bot.getCode());
        }

        final List<Competitor> missedCompetitors = serverPort.getMissedCompetitors(requiredCompetitors);
        for (Competitor competitor : missedCompetitors) {
            registerCompetitor(competitor, competitorsCode.get(competitor.getName() + competitor.getVersion()));
        }
    }

    private void registerCompetitor(Competitor competitor, byte[] code) {
        serverPort.registerCode(competitor, code);
    }

    private void enqueueBattle(BattleRequest request) {
        request.remoteId = serverPort.executeBattle(request.competitors, request.bfSpec, request.rounds, authToken);
        enqueuedRequests.add(request);

        request.currentRound = -1;
        request.state = new BattleRequestState();
        request.state.setMessage("Sended");
        request.state.setState(State.RECEIVED);

        battleRequestManager.battleRequestSubmitted(request);

        notifyListeners();
    }

    private void checkRequestsState() {
        boolean updated = false;
        for (BattleRequest request : enqueuedRequests) {
            final BattleRequestState prevState = request.state;
            final BattleRequestState state = serverPort.getState(request.remoteId);
            request.state = state;
            if (state.getState() == State.EXECUTED) {
                request.battleResults = serverPort.getBattleResults(request.remoteId);
            } else if (state.getMessage().startsWith("Round")) {
                if (request.currentRound == -1) {
                    request.requestStartExecutingTime = System.currentTimeMillis();
                }
                request.currentRound = Integer.parseInt(state.getMessage().split(" ")[1]);
            }

            if (prevState == null || !prevState.getMessage().equals(state.getMessage())) {
                updated = true;
                battleRequestManager.battleRequestStateUpdated(request);
            }
        }

        if (updated) {
            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (ProxyListener listener : listeners) {
            listener.proxyStateUpdate();
        }
    }

    public BattleRequest getCurrentRequest() {
        if (enqueuedRequests.size() == 0) {
            return null;
        }

        return enqueuedRequests.get(0);
    }
}
