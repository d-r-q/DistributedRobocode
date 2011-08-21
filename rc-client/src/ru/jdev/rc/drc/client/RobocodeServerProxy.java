package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.*;

import java.util.*;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class RobocodeServerProxy {

    private final Map<Integer, BattleRequest> enqueuedBattleRequests = new HashMap<>();
    private final Set<BattleRequest> executedBattleRequestsBuffer = new HashSet<>();

    private final RobocodeServer serverPort;
    private final String authToken;
    private final String url;

    private Integer currentBattleRequestId = null;

    public RobocodeServerProxy(RobocodeServer serverPort, String authToken, String url) {
        this.serverPort = serverPort;
        this.authToken = authToken;
        this.url = url;
    }

    public synchronized void enqueueBattle(BattleRequest request) {
        if (currentBattleRequestId != null) {
            throw new IllegalStateException("Current battle execution in process");
        }

        currentBattleRequestId = serverPort.executeBattle(request.competitors, request.bfSpec, request.rounds, authToken);
        request.remoteId = currentBattleRequestId;

        enqueuedBattleRequests.put(request.remoteId, request);
    }

    public boolean ready() {
        if (currentBattleRequestId == null) {
            return true;
        }

        checkCurrentRequestState();

        return currentBattleRequestId == null;
    }

    private void checkCurrentRequestState() {
        final BattleRequestState state = serverPort.getState(currentBattleRequestId);
        if (state.getState() == State.EXECUTED) {
            final BattleRequest battleRequest = enqueuedBattleRequests.remove(currentBattleRequestId);
            battleRequest.battleResults = serverPort.getBattleResults(currentBattleRequestId);
            executedBattleRequestsBuffer.add(battleRequest);
            currentBattleRequestId = null;
        } else if (state.getState() == State.REJECTED) {
            currentBattleRequestId = null;
            enqueuedBattleRequests.remove(currentBattleRequestId);
        }
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
        if (currentBattleRequestId != null) {
            checkCurrentRequestState();
        }

        return executedBattleRequestsBuffer.size() > 0;
    }

    @Override
    public String toString() {
        return url;
    }
}
