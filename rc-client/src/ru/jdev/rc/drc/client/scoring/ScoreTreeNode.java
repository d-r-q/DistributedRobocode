/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import ru.jdev.rc.drc.client.util.AvgValue;

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
    public double getScore(ScoreType scoreType) {
        final AvgValue avgScore = new AvgValue(children.size());

        for (AbstractScoreTreeNode child : children) {
            final double score = child.getScore(scoreType);
            if (!Double.isNaN(score)) {
                avgScore.addValue(score);
            }
        }

        return avgScore.getCurrentValue();
    }

    public List<AbstractScoreTreeNode> getChildren() {
        return children;
    }
}
