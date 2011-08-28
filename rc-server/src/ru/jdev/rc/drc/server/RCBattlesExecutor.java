/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.*;

import java.io.File;

public class RCBattlesExecutor implements IBattleListener {

    private RobocodeEngine robocodeEngine;

    private BattleResults[] currentBattleResults;
    private BattleRequest currentBattleRequest;

    private long finishTime;

    public RCBattlesExecutor() {
        this.robocodeEngine = new RobocodeEngine(new File(".\\rc\\"));
        robocodeEngine.addBattleListener(this);
    }

    public synchronized boolean startBattle(BattleRequest battleRequest) {
        System.out.println("Not working time: " + (System.currentTimeMillis() - finishTime));
        currentBattleRequest = battleRequest;
        currentBattleResults = null;

        final RobotSpecification[] robotSpecs = getRobotSpecs(battleRequest.competitors);
        for (RobotSpecification spec : robotSpecs) {
            if (spec == null) {
                currentBattleRequest.state.setState(BattleRequestState.State.REJECTED);
                currentBattleRequest.state.setMessage("Cannot find specs for all competitors");
                return false;
            }
        }
        final BattleSpecification battleSpecification = new BattleSpecification(battleRequest.rounds,
                new BattlefieldSpecification(battleRequest.bfSpec.getBfWidth(), battleRequest.bfSpec.getBfHeight()), robotSpecs);
        currentBattleRequest.state.setState(BattleRequestState.State.EXECUTING);
        currentBattleRequest.state.setMessage("Starting battle");
        robocodeEngine.runBattle(battleSpecification);
        System.out.printf("Executing battle %s vs %s (%d)\n", robotSpecs[0].getNameAndVersion(), robotSpecs[1].getNameAndVersion(),  + System.currentTimeMillis());

        return true;
    }

    public RSBattleResults getResults() {
        robocodeEngine.waitTillBattleOver();

        if (currentBattleResults == null) {
            currentBattleRequest.state.setMessage("Battle cancelled");
            currentBattleRequest.state.setState(BattleRequestState.State.REJECTED);
            return null;
        }

        CompetitorResults[] compRess = new CompetitorResults[currentBattleResults.length];
        int idx = 0;
        for (BattleResults br : currentBattleResults) {
            compRess[idx] = new CompetitorResults(br.getFirsts(), br.getScore(), br.getBulletDamage());
            idx++;
        }

        finishTime = System.currentTimeMillis();

        return new RSBattleResults(compRess);
    }

    private RobotSpecification[] getRobotSpecs(Competitor[] competitors) {
        final StringBuilder specs = new StringBuilder();
        for (Competitor competitor : competitors) {
            specs.append(competitor.name).append(' ').append(competitor.version).append(',');
        }
        specs.deleteCharAt(specs.length() - 1);
        return robocodeEngine.getLocalRepository(specs.toString());
    }

    public synchronized boolean cancelIfStarted(Integer battleRequestId) {
        if (currentBattleRequest != null && currentBattleRequest.requestId == battleRequestId) {
            System.out.println("Aborting current battle");
            robocodeEngine.abortCurrentBattle();
            return true;
        }

        return false;
    }

    public void onBattleFinished(BattleFinishedEvent battleFinishedEvent) {
        System.out.println("Battle finished");
    }

    public void onBattleCompleted(BattleCompletedEvent battleCompletedEvent) {
        currentBattleResults = battleCompletedEvent.getIndexedResults();
    }

    public void onRoundStarted(RoundStartedEvent roundStartedEvent) {
        currentBattleRequest.state.setState(BattleRequestState.State.EXECUTING);
        currentBattleRequest.state.setMessage("Round: " + roundStartedEvent.getRound());
    }

    public void onBattleStarted(BattleStartedEvent battleStartedEvent) {
    }

    public void onBattlePaused(BattlePausedEvent battlePausedEvent) {
    }

    public void onBattleResumed(BattleResumedEvent battleResumedEvent) {
    }

    public void onRoundEnded(RoundEndedEvent roundEndedEvent) {
    }

    public void onTurnStarted(TurnStartedEvent turnStartedEvent) {
    }

    public void onTurnEnded(TurnEndedEvent turnEndedEvent) {
    }

    public void onBattleMessage(BattleMessageEvent battleMessageEvent) {
    }

    public void onBattleError(BattleErrorEvent battleErrorEvent) {
    }

}
