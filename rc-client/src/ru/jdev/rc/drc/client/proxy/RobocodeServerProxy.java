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

    private final String authToken;
    private final String url;
    private final Set<Bot> bots;
    private final ExecutorService executorService;
    private final BattleRequestManager battleRequestManager;

    private RobocodeServer serverPort;
    private Integer currentBattleRequestId = null;
    private String stateMessage;
    private boolean connected;
    private BattleRequest currentRequest;

    public RobocodeServerProxy(String authToken, String url, Set<Bot> bots, ExecutorService executorService,
                               BattleRequestManager battleRequestManager) {
        this.authToken = authToken;
        this.url = url;
        this.bots = bots;
        this.executorService = executorService;
        this.battleRequestManager = battleRequestManager;
    }

    public void connect() {
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

            BattleRequest battleRequest;
            while ((battleRequest = battleRequestManager.getBattleRequest()) != null) {
                enqueueBattle(battleRequest);

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    checkRequestState();
                    if (battleRequest.state.getState() == State.EXECUTED) {
                        battleRequestManager.battleRequestExecuted(battleRequest);
                        currentBattleRequestId = null;
                        break;
                    } else if (battleRequest.state.getState() == State.REJECTED) {
                        battleRequestManager.battleRequestRejected(battleRequest);
                        currentBattleRequestId = null;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            stateMessage = String.format("Server is unavailable");
        }
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
        if (currentBattleRequestId != null) {
            throw new IllegalStateException("Current battle execution in process");
        }

        currentBattleRequestId = serverPort.executeBattle(request.competitors, request.bfSpec, request.rounds, authToken);

        currentRequest = request;
        currentRequest.currentRound = -1;
        currentRequest.remoteId = currentBattleRequestId;
        currentRequest.state = new BattleRequestState();
        currentRequest.state.setMessage("Sended");
        currentRequest.state.setState(State.RECEIVED);

        notifyListeners();
    }

    private void checkRequestState() {
        final BattleRequestState prevState = currentRequest.state;
        final BattleRequestState state = serverPort.getState(currentBattleRequestId);
        currentRequest.state = state;
        if (state.getState() == State.EXECUTED) {
            currentRequest.battleResults = serverPort.getBattleResults(currentBattleRequestId);
        } else if (state.getMessage().startsWith("Round")) {
            if (currentRequest.currentRound == -1) {
                currentRequest.requestStartExecutingTime = System.currentTimeMillis();
            }
            currentRequest.currentRound = Integer.parseInt(state.getMessage().split(" ")[1]);
        }

        if (!prevState.getMessage().equals(state.getMessage())) {
            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (ProxyListener listener : listeners) {
            listener.proxyStateUpdate();
        }
    }

    public BattleRequest getCurrentRequest() {
        return currentRequest;
    }
}
