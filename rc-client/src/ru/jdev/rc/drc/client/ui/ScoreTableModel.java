/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.Challenge;
import ru.jdev.rc.drc.client.scoring.AbstractScoreTreeNode;
import ru.jdev.rc.drc.client.scoring.ScoreTreeLeaf;
import ru.jdev.rc.drc.client.scoring.ScoreType;

import javax.swing.table.AbstractTableModel;
import java.util.Iterator;
import java.util.List;

public class ScoreTableModel extends AbstractTableModel {

    private final List<AbstractScoreTreeNode> scores;

    public ScoreTableModel(Challenge challenge) {
        scores = challenge.getScoringTree().getFlat();
        if (scores.size() > 20) {
            for (Iterator<AbstractScoreTreeNode> nodesIter = scores.iterator(); nodesIter.hasNext(); ) {
                if (nodesIter.next() instanceof ScoreTreeLeaf) {
                    nodesIter.remove();
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
        } else {
            return scores.get(column - 1).getName();
        }
    }

    @Override
    public int getRowCount() {
        return ScoreType.values().length;
    }

    @Override
    public int getColumnCount() {
        return scores.size() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return ScoreType.values()[rowIndex].getScoreName();
        } else {
            return String.format("%3.2f", scores.get(columnIndex - 1).getScore(ScoreType.values()[rowIndex]));
        }
    }
}
