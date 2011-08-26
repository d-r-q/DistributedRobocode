/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.*;

import java.util.List;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class BattleRequest {

    public final List<Competitor> competitors;
    public final BfSpec bfSpec;
    public final int rounds;

    public int localId;
    public int remoteId;
    public RsBattleResults battleResults;
    public BattleRequestState state;
    public long requestStartExecutingTime;
    public int currentRound;

    public BattleRequest(List<Competitor> competitors, BfSpec bfSpec, int rounds) {
        this.competitors = competitors;
        this.bfSpec = bfSpec;
        this.rounds = rounds;
    }

    public double getChallengerAPS() {
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);
        final CompetitorResults rr = battleResults.getCompetitorResults().get(1);

        if (cr == null || rr == null) {
            return Double.NaN;
        }

        return ((double) cr.getScore() / (double) (cr.getScore() + rr.getScore())) * 100;
    }

    public int getChallengerScore() {
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);

        if (cr == null) {
            return -1;
        }

        return cr.getScore();
    }

    public int getChallengerBulletDamage() {
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);

        if (cr == null) {
            return -1;
        }

        return cr.getBulletDamage();
    }

    public int getReferenceScore() {
        final CompetitorResults cr = battleResults.getCompetitorResults().get(1);

        if (cr == null) {
            return -1;
        }

        return cr.getScore();
    }

    public int getReferenceBulletDamage() {
        final CompetitorResults cr = battleResults.getCompetitorResults().get(1);

        if (cr == null) {
            return -1;
        }

        return cr.getBulletDamage();
    }

    public int getTotalScore() {
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);
        final CompetitorResults rr = battleResults.getCompetitorResults().get(1);

        if (cr == null || rr == null) {
            return -1;
        }

        return cr.getScore() + rr.getScore();
    }

    @Override
    public String toString() {
        return String.valueOf(localId);
    }
}
