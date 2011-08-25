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
                if (!request.isCompetitorsLoaded) {
                    loadCompetitors(request);
                }
                rcBattlesExecutor.startBattle(request);
                codeManager.cleanup();
                prepareNextBattle();
                final RSBattleResults rsBattleResults = rcBattlesExecutor.getResults();
                if (rsBattleResults != null) {
                    rsBattleResults.requestId = request.requestId;
                    request.state.setState(BattleRequestState.State.EXECUTED);
                    request.state.setMessage("");
                }
                battleResultsBuffer.addBattleResult(request.getRequestId(), rsBattleResults);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareNextBattle() {
        final BattleRequest nextRequest = battleRequestsQueue.peak();
        if (nextRequest != null) {
            loadCompetitors(nextRequest);
        }
    }

    private void loadCompetitors(BattleRequest request) {
        request.state.setState(BattleRequestState.State.EXECUTING);
        for (Competitor competitor : request.competitors) {
            try {
                codeManager.loadCompetitor(competitor);
            } catch (IOException e) {
                request.state.setState(BattleRequestState.State.REJECTED);
                request.state.setMessage(e.getMessage());
            }
        }

        codeManager.reloadRobotsDataBase();
    }

    public void stop() {
        isRunned = false;
    }

}
