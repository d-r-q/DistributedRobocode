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
            try {
                final BattleRequest request = battleRequestsQueue.getBattleRequest(100);
                if (request == null) {
                    continue;
                }

                for (Competitor competitor : request.competitors) {
                    try {
                        codeManager.loadCompetitor(competitor);
                    } catch (IOException e) {
                        request.state = BattleRequestState.REJECTED;
                    }
                }
                request.state = BattleRequestState.EXECUTING;
                final RSBattleResults rsBattleResults = rcBattlesExecutor.executeBattle(request.competitors, request.bfSpec, request.rounds);
                request.state = BattleRequestState.EXECUTED;
                battleResultsBuffer.addBattleResult(request.getRequestId(), rsBattleResults);
            } catch (InterruptedException e) {
                isRunned = false;
            }
        }
    }

    public void stop() {
        isRunned = false;
    }

}
