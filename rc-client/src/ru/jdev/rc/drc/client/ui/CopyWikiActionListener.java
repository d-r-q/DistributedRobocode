package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.Challenge;
import ru.jdev.rc.drc.client.scoring.AbstractScoreTreeNode;
import ru.jdev.rc.drc.client.scoring.ScoreTreeLeaf;
import ru.jdev.rc.drc.client.scoring.ScoreType;

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

    public CopyWikiActionListener(Challenge challenge) {
        this.challenge = challenge;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final StringBuilder wikiReults = new StringBuilder(String.format("|-\n" +
                "| %s || [[Author]] || Algorithm ||", challenge.getChallenger().getNameAndVersion()));
        for (AbstractScoreTreeNode node : challenge.getScoringTree().getFlat()) {
            if (node instanceof ScoreTreeLeaf) {
                wikiReults.append(String.format("%.2f ||", node.getScore(ScoreType.AVERAGED_PERCENTS_SCORE)));
            } else {
                wikiReults.append(String.format("'''%.2f''' ||", node.getScore(ScoreType.AVERAGED_PERCENTS_SCORE)));
            }
        }

        wikiReults.append(String.format("%s seasons", challenge.getSeasons()));
        StringSelection stringSelection = new StringSelection(wikiReults.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
    }
}
