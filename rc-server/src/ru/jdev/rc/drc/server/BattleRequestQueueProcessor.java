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
            System.out.println("Battle fetched " + System.currentTimeMillis());

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
                System.out.println("Storing battle results");
                battleResultsBuffer.addBattleResult(request.getRequestId(), rsBattleResults);
                System.out.println("Battle results stored " + System.currentTimeMillis());
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
        System.out.println("Load competitors...");
        request.state.setState(BattleRequestState.State.EXECUTING);
        request.isCompetitorsLoaded = true;
        for (Competitor competitor : request.competitors) {
            try {
                codeManager.loadCompetitor(competitor);
            } catch (IOException e) {
                request.state.setState(BattleRequestState.State.REJECTED);
                request.state.setMessage(e.getMessage());
                request.isCompetitorsLoaded = false;
            }
        }
        System.out.println("Loaded");

        System.out.println("Reloading robots database...");
        codeManager.reloadRobotsDataBase();
        System.out.println("Reloaded");
    }

    public void stop() {
        isRunned = false;
    }

}
