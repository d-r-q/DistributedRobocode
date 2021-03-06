/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.util.AvgValue;
import ru.jdev.rc.drc.client.util.Median;

import java.util.ArrayList;
import java.util.List;

public class ScoreTreeLeaf extends AbstractScoreTreeNode {

    private final List<BattleRequest> battleRequests = new ArrayList<>();
    private final ScoreTreeNode parent;

    public ScoreTreeLeaf(String name, ScoreTreeNode parent) {
        super(name);
        this.parent = parent;
    }

    public void addBattleRequest(BattleRequest battleRequest) {
        battleRequests.add(battleRequest);
    }

    public ScoreTreeNode getParent() {
        return parent;
    }

    @Override
    public double getAvgScore(ScoreType scoreType) {
        final AvgValue avgScore = new AvgValue(battleRequests.size());

        for (BattleRequest request : battleRequests) {
            avgScore.addValue(scoreType.getScore(request));
        }

        return avgScore.getCurrentValue();
    }

    public double getMedScore(ScoreType scoreType) {
        final Median medScore = new Median(battleRequests.size());

        for (BattleRequest request : battleRequests) {
            medScore.addValue(scoreType.getScore(request));
        }

        return medScore.getMedian();
    }

    public List<BattleRequest> getBattleRequests() {
        return battleRequests;
    }
}
