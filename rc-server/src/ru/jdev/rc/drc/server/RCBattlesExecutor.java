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

    private final RobocodeEngine robocodeEngine;

    private BattleResults[] currentBattleResults;
    private BattleRequest currentBattleRequest;

    public RCBattlesExecutor() {
        this.robocodeEngine = new RobocodeEngine(new File(".\\rc\\"));
        robocodeEngine.addBattleListener(this);
    }

    public synchronized RSBattleResults executeBattle(BattleRequest battleRequest) {
        currentBattleRequest = battleRequest;
        currentBattleResults = null;

        final BattleSpecification battleSpecification = new BattleSpecification(battleRequest.rounds,
                new BattlefieldSpecification(battleRequest.bfSpec.getBfWidth(), battleRequest.bfSpec.getBfHeight()), getRobotSpecs(battleRequest.competitors));
        currentBattleRequest.state.setState(BattleRequestState.State.EXECUTING);
        currentBattleRequest.state.setMessage("Starting battle");
        robocodeEngine.runBattle(battleSpecification);
        robocodeEngine.waitTillBattleOver();

        CompetitorResults[] compRess = new CompetitorResults[currentBattleResults.length];
        int idx = 0;
        for (BattleResults br : currentBattleResults) {
            compRess[idx] = new CompetitorResults(br.getFirsts(), br.getScore(), br.getBulletDamage());
            idx++;
        }

        return new RSBattleResults(compRess);
    }

    private RobotSpecification[] getRobotSpecs(Competitor[] competitors) {
        final RobotSpecification[] specs = new RobotSpecification[competitors.length];

        int specsIdx = 0;
        final RobotSpecification[] localRepository = robocodeEngine.getLocalRepository();
        for (Competitor competitor : competitors) {
            for (RobotSpecification spec : localRepository) {
                if (spec.getNameAndVersion().equals(competitor.name + "* " + competitor.version) ||
                        spec.getNameAndVersion().equals(competitor.name + " " + competitor.version)) {
                    specs[specsIdx++] = spec;
                }
            }
        }

        return specs;
    }

    public void onBattleStarted(BattleStartedEvent battleStartedEvent) {
        System.out.println("Battle started");
    }

    public void onBattleFinished(BattleFinishedEvent battleFinishedEvent) {
        System.out.println("Battle finished");
    }

    public void onBattleCompleted(BattleCompletedEvent battleCompletedEvent) {
        currentBattleResults = battleCompletedEvent.getSortedResults();
        System.out.println("Battle completed");
    }

    public void onBattlePaused(BattlePausedEvent battlePausedEvent) {
    }

    public void onBattleResumed(BattleResumedEvent battleResumedEvent) {
    }

    public void onRoundStarted(RoundStartedEvent roundStartedEvent) {
        currentBattleRequest.state.setState(BattleRequestState.State.EXECUTING);
        currentBattleRequest.state.setMessage("Round: " + roundStartedEvent.getRound());
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
