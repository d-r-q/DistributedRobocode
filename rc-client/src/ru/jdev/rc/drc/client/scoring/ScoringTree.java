/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import java.util.ArrayList;
import java.util.List;

public class ScoringTree {

    private final ScoreTreeNode root;

    public ScoringTree(ScoreTreeNode root) {
        this.root = root;
    }

    public List<AbstractScoreTreeNode> getFlat() {
        return getFlat(root);
    }

    private List<AbstractScoreTreeNode> getFlat(AbstractScoreTreeNode node) {
        final List<AbstractScoreTreeNode> flatNodes = new ArrayList<>();

        if (node instanceof ScoreTreeLeaf) {
            flatNodes.add(node);
        } else {
            for (AbstractScoreTreeNode child : ((ScoreTreeNode) node).getChildren()) {
                flatNodes.addAll(getFlat(child));
            }
            flatNodes.add(node);
        }

        return flatNodes;
    }

}
