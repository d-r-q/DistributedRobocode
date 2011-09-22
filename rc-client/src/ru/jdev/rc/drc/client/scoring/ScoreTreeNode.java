/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import ru.jdev.rc.drc.client.util.AvgValue;
import ru.jdev.rc.drc.client.util.Median;

import java.util.ArrayList;
import java.util.List;

public class ScoreTreeNode extends AbstractScoreTreeNode {

    private final List<AbstractScoreTreeNode> children = new ArrayList<>();

    public ScoreTreeNode(String name) {
        super(name);
    }

    public void addChild(AbstractScoreTreeNode child) {
        children.add(child);
    }

    @Override
    public double getAvgScore(ScoreType scoreType) {
        final AvgValue avgScore = new AvgValue(children.size());

        for (AbstractScoreTreeNode child : children) {
            final double score = child.getAvgScore(scoreType);
            if (!Double.isNaN(score)) {
                avgScore.addValue(score);
            }
        }

        return avgScore.getCurrentValue();
    }

    public double getMedScore(ScoreType scoreType) {
        final Median medScore = new Median(children.size());

        for (AbstractScoreTreeNode child : children) {
            final double score = child.getMedScore(scoreType);
            if (!Double.isNaN(score)) {
                medScore.addValue(score);
            }
        }

        return medScore.getMedian();
    }

    public List<AbstractScoreTreeNode> getChildren() {
        return children;
    }
}
