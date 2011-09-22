/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.Challenge;
import ru.jdev.rc.drc.client.scoring.AbstractScoreTreeNode;
import ru.jdev.rc.drc.client.scoring.ScoreTreeLeaf;
import ru.jdev.rc.drc.client.scoring.ScoreType;
import ru.jdev.rc.drc.client.util.AvgValue;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScoreTableModel extends AbstractTableModel {

    private final List<AbstractScoreTreeNode> visibleScores;
    private final List<ScoreTreeLeaf> leafs;

    public ScoreTableModel(Challenge challenge) {
        visibleScores = challenge.getScoringTree().getFlat();
        leafs = new ArrayList<>();
        if (visibleScores.size() > 20) {
            for (Iterator<AbstractScoreTreeNode> nodesIter = visibleScores.iterator(); nodesIter.hasNext(); ) {
                final AbstractScoreTreeNode next = nodesIter.next();
                if (next instanceof ScoreTreeLeaf) {
                    nodesIter.remove();
                    leafs.add((ScoreTreeLeaf) next);
                }
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return double.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Type";
        } else if (column - 1 < visibleScores.size()) {
            return visibleScores.get(column - 1).getName();
        } else {
            return "Raw total";
        }
    }

    @Override
    public int getRowCount() {
        return ScoreType.values().length;
    }

    @Override
    public int getColumnCount() {
        return visibleScores.size() + 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return ScoreType.values()[rowIndex].getScoreName();
        } else if (columnIndex == visibleScores.size() + 1) {
            final AvgValue avg = new AvgValue(1000);
            for (ScoreTreeLeaf leaf : leafs) {
                for (BattleRequest br : leaf.getBattleRequests()) {
                    avg.addValue((ScoreType.values()[rowIndex]).getScore(br));
                }
            }

            return String.format("%3.2f", avg.getCurrentValue());
        } else {
            return String.format("%3.2f", visibleScores.get(columnIndex - 1).getAvgScore(ScoreType.values()[rowIndex]));
        }
    }
}
