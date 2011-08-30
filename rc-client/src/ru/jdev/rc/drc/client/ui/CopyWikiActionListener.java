/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.Challenge;
import ru.jdev.rc.drc.client.scoring.AbstractScoreTreeNode;
import ru.jdev.rc.drc.client.scoring.ScoreTreeLeaf;
import ru.jdev.rc.drc.client.scoring.ScoreType;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: jdev
 * Date: 29.08.11
 */
public class CopyWikiActionListener implements ActionListener {

    private final Challenge challenge;
    private final ListSelectionModel selectionModel;

    public CopyWikiActionListener(Challenge challenge, ListSelectionModel selectionModel) {
        this.challenge = challenge;
        this.selectionModel = selectionModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectionModel.getMaxSelectionIndex() == -1) {
            JOptionPane.showMessageDialog(null, "No Scoring type selected\n Copy results select any row in Scores table", "Copy error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final ScoreType selectedScoreType = ScoreType.values()[selectionModel.getMaxSelectionIndex()];

        final StringBuilder wikiReults = getWikiStr(challenge, selectedScoreType);
        StringSelection stringSelection = new StringSelection(wikiReults.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
    }

    // todo(zhidkov): move right place
    public static StringBuilder getWikiStr(Challenge challenge, ScoreType selectedScoreType) {
        final StringBuilder wikiReults = new StringBuilder(String.format("|-\n" +
                "| [[%s]] %s || [[Author]] || Algorithm ||", challenge.getChallenger().getBotName(), challenge.getChallenger().getBotVersion()));
        for (AbstractScoreTreeNode node : challenge.getScoringTree().getFlat()) {
            if (node instanceof ScoreTreeLeaf) {
                if (((ScoreTreeLeaf) node).getParent().getChildren().size() > 1) {
                    wikiReults.append(String.format("%.2f ||", node.getScore(selectedScoreType)));
                }
            } else {
                wikiReults.append(String.format("'''%.2f''' ||", node.getScore(selectedScoreType)));
            }
        }

        wikiReults.append(String.format("%s seasons", challenge.getSeasons()));
        return wikiReults;
    }
}
