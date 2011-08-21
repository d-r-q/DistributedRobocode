/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.IOException;

public class BattleRequestQueueProcessor implements Runnable {

    private final BattleRequestsQueue battleRequestsQueue;
    private final RCBattlesExecutor rcBattlesExecutor;
    private final CodeManager codeManager;
    private final BattleResultsBuffer battleResultsBuffer;

    private volatile boolean isRunned = true;

    public BattleRequestQueueProcessor(BattleRequestsQueue battleRequestsQueue,
                                       RCBattlesExecutor rcBattlesExecutor, CodeManager codeManager, BattleResultsBuffer battleResultsBuffer) {
        this.battleRequestsQueue = battleRequestsQueue;
        this.rcBattlesExecutor = rcBattlesExecutor;
        this.codeManager = codeManager;
        this.battleResultsBuffer = battleResultsBuffer;
    }

    public void run() {
        while (isRunned && !Thread.interrupted()) {
            final BattleRequest request;
            try {
                request = battleRequestsQueue.getBattleRequest(100);
            } catch (InterruptedException e) {
                isRunned = false;
                continue;
            }
            if (request == null) {
                continue;
            }

            try {
                loadCompetitors(request);
                request.state = BattleRequestState.EXECUTING;
                final RSBattleResults rsBattleResults = rcBattlesExecutor.executeBattle(request.competitors, request.bfSpec, request.rounds);
                rsBattleResults.requestId = request.requestId;
                request.state = BattleRequestState.EXECUTED;
                battleResultsBuffer.addBattleResult(request.getRequestId(), rsBattleResults);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                codeManager.cleanup();
            }
        }
    }

    private void loadCompetitors(BattleRequest request) {
        for (Competitor competitor : request.competitors) {
            try {
                codeManager.loadCompetitor(competitor);
            } catch (IOException e) {
                request.state = BattleRequestState.REJECTED;
            }
        }
    }

    public void stop() {
        isRunned = false;
    }

}
