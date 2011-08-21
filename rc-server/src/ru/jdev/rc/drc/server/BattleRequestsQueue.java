/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.max;

public class BattleRequestsQueue {

    private final List<BattleRequest> priorityQueue = new LinkedList<>();
    private final List<BattleRequest> commonQueue = new LinkedList<>();

    private final String secureToken;

    public BattleRequestsQueue(String secureToken) {
        this.secureToken = secureToken;
    }

    public synchronized void addBattleRequest(BattleRequest battleRequest) {
        int queueOrder;
        if (battleRequest.secureToken.equals(secureToken)) {
            priorityQueue.add(battleRequest);
            queueOrder = priorityQueue.size();
        } else {
            commonQueue.add(battleRequest);
            queueOrder = priorityQueue.size() + commonQueue.size();
        }
        battleRequest.state.setState(BattleRequestState.State.QUEUED);
        battleRequest.state.setMessage("Queue order: " + queueOrder);

        notify();
    }

    public synchronized BattleRequest getBattleRequest(long timeout) throws InterruptedException {
        final long timeLimit = System.currentTimeMillis() + timeout;
        while (priorityQueue.size() == 0 && commonQueue.size() == 0 && System.currentTimeMillis() < timeLimit) {
            wait(max(timeLimit - System.currentTimeMillis(), 1));
        }

        final BattleRequest res = getBattleRequest();

        resetQueueOrder();

        return res;
    }

    private BattleRequest getBattleRequest() {
        final BattleRequest res;
        if (priorityQueue.size() > 0) {
            res = priorityQueue.remove(0);
        } else if (commonQueue.size() > 0) {
            res = commonQueue.remove(0);
        } else {
            res = null;
        }
        return res;
    }

    private void resetQueueOrder() {
        int queueOrder = 1;
        for (BattleRequest request : priorityQueue) {
            request.state.setState(BattleRequestState.State.QUEUED);
            request.state.setMessage("Queue order: " + queueOrder);
            queueOrder++;
        }

        for (BattleRequest request : commonQueue) {
            request.state.setState(BattleRequestState.State.QUEUED);
            request.state.setMessage("Queue order: " + queueOrder);
            queueOrder++;
        }
    }

}
