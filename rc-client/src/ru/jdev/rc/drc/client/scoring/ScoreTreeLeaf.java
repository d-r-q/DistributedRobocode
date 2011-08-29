/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.util.AvgValue;

import java.util.ArrayList;
import java.util.List;

public class ScoreTreeLeaf extends AbstractScoreTreeNode {

    private final List<BattleRequest> battleRequests = new ArrayList<>();

    public ScoreTreeLeaf(String name) {
        super(name);
    }

    public void addBattleRequest(BattleRequest battleRequest) {
        battleRequests.add(battleRequest);
    }

    @Override
    public double getScore(ScoreType scoreType) {
        final AvgValue avgScore = new AvgValue(battleRequests.size());

        for (BattleRequest request : battleRequests) {
            avgScore.addValue(scoreType.getScore(request));
        }

        return avgScore.getCurrentValue();
    }
}
