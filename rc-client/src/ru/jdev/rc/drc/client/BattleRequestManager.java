/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.client.ui.QueuePanel;
import ru.jdev.rc.drc.server.BfSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: jdev
 * Date: 21.08.11
 */
public class BattleRequestManager {

    private final List<BattleRequestManagerListener> listeners = new ArrayList<>();

    private final List<BattleRequest> pendingRequests = new ArrayList<>();
    private final List<BattleRequest> executingRequests = new ArrayList<>();
    private final List<BattleRequest> executedRequests = new ArrayList<>();

    private int totalRequests;

    public BattleRequestManager(Challenge challenge, int seasons) {
        final BfSpec battlefieldSpecification = new BfSpec();
        battlefieldSpecification.setBfWidth(800);
        battlefieldSpecification.setBfHeight(600);

        int idSeq = 0;
        for (int i = 0; i < seasons; i++) {
            for (BotsGroup botsGroup : challenge.getBotGroups()) {
                for (Bot bot : botsGroup.getBots()) {
                    final BattleRequest battleRequest = new BattleRequest(Arrays.asList(challenge.getChallenger().getCompetitor(), bot.getCompetitor()), battlefieldSpecification, challenge.getRounds());
                    battleRequest.localId = idSeq++;
                    pendingRequests.add(battleRequest);
                }
            }
        }

        totalRequests = pendingRequests.size();
    }

    public synchronized BattleRequest getBattleRequest() {
        if (pendingRequests.size() > 0) {
            return pendingRequests.remove(0);
        }

        return null;
    }

    public synchronized void battleRequestSubmitted(BattleRequest request) {
        executingRequests.add(request);
        notifyListeners(request, Event.SUBMITTED);
    }

    public synchronized void battleRequestExecuted(BattleRequest battleRequest) {
        executedRequests.add(battleRequest);
        executingRequests.remove(battleRequest);
        notifyListeners(battleRequest, Event.EXECUTED);
    }

    @Override
    public String toString() {
        return "pendingRequests: " + pendingRequests + "\n" +
                "executingRequests: " + executingRequests + "\n" +
                "executedRequests: " + executedRequests;
    }

    public boolean hasNotExecutedRequests() {
        return pendingRequests.size() > 0 || executingRequests.size() > 0;
    }

    public boolean hasPendingRequests() {
        return pendingRequests.size() > 0;
    }

    public int getExecutedBattleRequests() {
        return executedRequests.size();
    }

    public int getRemainingBattleRequests() {
        return pendingRequests.size() + executingRequests.size();
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public synchronized State getState() {
        return new State(new ArrayList<>(pendingRequests), new ArrayList<>(executingRequests), new ArrayList<>(executedRequests));
    }

    public double getAps() {
        int totalScore = 0;
        int challengerScore = 0;

        for (BattleRequest br : executedRequests) {
            challengerScore += br.battleResults.getCompetitorResults().get(0).getScore();
            totalScore += br.battleResults.getCompetitorResults().get(0).getScore() + br.battleResults.getCompetitorResults().get(1).getScore();
        }

        return ((double) challengerScore) / totalScore * 100;
    }

    public void battleRequestRejected(BattleRequest battleRequest) {
        executingRequests.remove(battleRequest);
        pendingRequests.add(battleRequest);
        notifyListeners(battleRequest, Event.EXECUTION_REJECTED);
    }

    public List<BattleRequest> getPendingRequests() {
        return pendingRequests;
    }

    public void addListener(BattleRequestManagerListener listener) {
        listeners.add(listener);
    }

    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        notifyListeners(battleRequest, Event.STATE_UPDATED);
    }

    public class State {

        public List<BattleRequest> pendingRequests;
        public List<BattleRequest> executingRequests;
        public List<BattleRequest> executedRequests;

        public State(List<BattleRequest> pendingRequests, List<BattleRequest> executingRequests, List<BattleRequest> executedRequests) {
            this.pendingRequests = pendingRequests;
            this.executingRequests = executingRequests;
            this.executedRequests = executedRequests;
        }
    }

    private void notifyListeners(BattleRequest battleRequest, Event event) {
        for (BattleRequestManagerListener listener : listeners) {
            switch (event) {
                case SUBMITTED:
                    listener.battleRequestSubmitted(battleRequest);
                    break;
                case EXECUTION_REJECTED:
                    listener.battleRequestExecutionRejected(battleRequest);
                    break;
                case EXECUTED:
                    listener.battleRequestExecuted(battleRequest);
                    break;
                case STATE_UPDATED:
                    listener.battleRequestStateUpdated(battleRequest);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported event: " + event);
            }
        }
    }

    private enum Event {
        SUBMITTED,
        EXECUTION_REJECTED,
        EXECUTED,
        STATE_UPDATED
    }

}
