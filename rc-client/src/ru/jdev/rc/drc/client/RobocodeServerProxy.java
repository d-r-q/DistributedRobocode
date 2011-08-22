/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.BattleRequestState;
import ru.jdev.rc.drc.server.Competitor;
import ru.jdev.rc.drc.server.RobocodeServer;
import ru.jdev.rc.drc.server.State;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class RobocodeServerProxy {

    private final Map<Integer, BattleRequest> enqueuedBattleRequests = new HashMap<>();
    private final Set<BattleRequest> executedBattleRequestsBuffer = new HashSet<>();

    private final String authToken;
    private final String url;
    private final Set<Bot> bots;

    private RobocodeServer serverPort;
    private Future<RobocodeServer> connectingFuture;
    private Integer currentBattleRequestId = null;
    private String stateMessage;
    private boolean connected;

    public RobocodeServerProxy(String authToken, String url, Set<Bot> bots) {
        this.authToken = authToken;
        this.url = url;
        this.bots = bots;
    }

    public synchronized void enqueueBattle(BattleRequest request) {
        if (currentBattleRequestId != null) {
            throw new IllegalStateException("Current battle execution in process");
        }

        currentBattleRequestId = serverPort.executeBattle(request.competitors, request.bfSpec, request.rounds, authToken);
        request.remoteId = currentBattleRequestId;

        enqueuedBattleRequests.put(request.remoteId, request);
    }

    public synchronized boolean ready() {
        return serverPort != null && currentBattleRequestId == null;

    }

    public synchronized boolean checkState() {
        if (connectingFuture != null) {
            if (connectingFuture.isDone()) {
                try {
                    serverPort = connectingFuture.get();
                    connectingFuture = null;
                    if (serverPort != null) {
                        loadCompetitors();
                        connected = true;
                        stateMessage = "Connected";
                        return true;
                    } else {
                        stateMessage = "Server is unavailable";
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (currentBattleRequestId == null) {
                return true;
            }
            final BattleRequestState state = serverPort.getState(currentBattleRequestId);
            enqueuedBattleRequests.get(currentBattleRequestId).state = state;
            if (state.getState() == State.EXECUTED) {
                final BattleRequest battleRequest = enqueuedBattleRequests.remove(currentBattleRequestId);
                battleRequest.battleResults = serverPort.getBattleResults(currentBattleRequestId);
                executedBattleRequestsBuffer.add(battleRequest);
                currentBattleRequestId = null;
                return true;
            } else if (state.getState() == State.REJECTED) {
                currentBattleRequestId = null;
                enqueuedBattleRequests.remove(currentBattleRequestId);
                return true;
            }
        }

        return false;
    }

    private void loadCompetitors() {
        final List<Competitor> requiredCompetitors = new ArrayList<>();
        final Map<String, byte[]> competitorsCode = new HashMap<>();
        for (Bot bot : bots) {
            requiredCompetitors.add(bot.getCompetitor());
            competitorsCode.put(bot.getBotName() + bot.getBotVersion(), bot.getCode());
        }

        stateMessage = "Checking for missed competitors";
        final List<Competitor> missedCompetitors = serverPort.getMissedCompetitors(requiredCompetitors);
        for (Competitor competitor : missedCompetitors) {
            registerCompetitor(competitor, competitorsCode.get(competitor.getName() + competitor.getVersion()));
        }
    }

    private void registerCompetitor(Competitor competitor, byte[] code) {
        stateMessage = String.format("Registering competitor %s %s\n", competitor.getName(), competitor.getVersion());
        serverPort.registerCode(competitor, code);
    }

    public Collection<BattleRequest> flushExecutedBattleRequestsBuffer() {
        final Set<BattleRequest> requestsBuffer = new HashSet<>(executedBattleRequestsBuffer);
        executedBattleRequestsBuffer.clear();
        return requestsBuffer;
    }

    public RobocodeServer getServerPort() {
        return serverPort;
    }

    public boolean hasResults() {
        return executedBattleRequestsBuffer.size() > 0;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return url;
    }

    public synchronized BattleRequest getCurrentBattleRequest() {
        return enqueuedBattleRequests.get(currentBattleRequestId);
    }

    public void setConnectingFuture(Future<RobocodeServer> connectingFuture) {
        stateMessage = "Connecting";
        this.connectingFuture = connectingFuture;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public boolean isConnected() {
        return connected;
    }
}
