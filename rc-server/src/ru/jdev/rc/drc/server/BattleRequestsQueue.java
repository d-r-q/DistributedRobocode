/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.util.Iterator;
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

    public synchronized BattleRequest getBattleRequest() {
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

    public synchronized BattleRequest peak() {
        final BattleRequest res;
        if (priorityQueue.size() > 0) {
            res = priorityQueue.get(0);
        } else if (commonQueue.size() > 0) {
            res = commonQueue.get(0);
        } else {
            res = null;
        }
        return res;
    }

    public synchronized boolean remove(Integer battleRequestId) {
        boolean isRemoved = false;
        for (Iterator<BattleRequest> priorityQueueIter = priorityQueue.iterator(); priorityQueueIter.hasNext(); ) {
            if (priorityQueueIter.next().requestId == battleRequestId) {
                priorityQueueIter.remove();
                isRemoved = true;
                break;
            }
        }

        for (Iterator<BattleRequest> commonQueueIter = commonQueue.iterator(); commonQueueIter.hasNext(); ) {
            if (commonQueueIter.next().requestId == battleRequestId) {
                commonQueueIter.remove();
                isRemoved = true;
                break;
            }
        }

        if (isRemoved) {
            resetQueueOrder();
        }

        return isRemoved;
    }
}
