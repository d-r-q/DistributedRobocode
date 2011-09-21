/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.*;
import ru.jdev.rc.drc.client.proxy.ProxyList;
import ru.jdev.rc.drc.client.proxy.RobocodeServerProxy;
import ru.jdev.rc.drc.client.scoring.AbstractScoreTreeNode;
import ru.jdev.rc.drc.client.scoring.ScoreTreeLeaf;
import ru.jdev.rc.drc.client.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RCCFrame extends JFrame implements BattleRequestManagerListener, WindowListener {

    private final BattleRequestManager battleRequestManager;
    private final ProxyList proxyList;
    private final ExecutorService executorService;
    private final Challenge challenge;

    private final JPanel challengeResults = new JPanel();
    private final JPanel serversPanel = new JPanel();
    private final InfoPanel infoPanel;

    private JTable resultsTable;
    private QueuePanel queuePanel;
    private List<AbstractScoreTreeNode> flatNodes;
    private JTable scoresTable;
    private JScrollPane resultsTableScrollPane;

    public RCCFrame(BattleRequestManager battleRequestManager, ProxyList proxyList, RobocodeClient robocodeClient, ExecutorService executorService, Challenge challenge) throws HeadlessException {
        this.battleRequestManager = battleRequestManager;
        this.proxyList = proxyList;
        this.executorService = executorService;
        this.challenge = challenge;
        this.infoPanel = new InfoPanel(challenge, robocodeClient);
        flatNodes = challenge.getScoringTree().getFlat();
    }

    public void init() {
        addWindowListener(this);
        battleRequestManager.addListener(this);

        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        queuePanel = new QueuePanel(battleRequestManager.getPendingRequests());
        queuePanel.setPreferredSize(new Dimension(270, Integer.MAX_VALUE));
        getContentPane().add(queuePanel, BorderLayout.WEST);
        getContentPane().add(challengeResults, BorderLayout.CENTER);
        getContentPane().add(serversPanel, BorderLayout.EAST);
        serversPanel.setPreferredSize(new Dimension(220, 1000));
        serversPanel.setLayout(new BoxLayout(serversPanel, BoxLayout.Y_AXIS));

        for (RobocodeServerProxy proxy : proxyList.getAvailableProxies()) {
            final ServerPanel serverPanel = new ServerPanel(proxy);
            serverPanel.init();
            proxy.addListener(serverPanel);
            serversPanel.add(serverPanel);
        }

        challengeResults.setLayout(new BoxLayout(challengeResults, BoxLayout.Y_AXIS));
        resultsTable = new JTable(new BattleRequestsTableModel(new BattleRequestsTableModel.BattleRequestColumn[]
                {BattleRequestsTableModel.BattleRequestColumn.referenceBotName,
                        BattleRequestsTableModel.BattleRequestColumn.challengerAps,
                        BattleRequestsTableModel.BattleRequestColumn.challengerScoreGainRate,
                        BattleRequestsTableModel.BattleRequestColumn.challengerEnergyConserved,
                        BattleRequestsTableModel.BattleRequestColumn.challengerScore,
                        BattleRequestsTableModel.BattleRequestColumn.challengerBulletDamage,
                        BattleRequestsTableModel.BattleRequestColumn.referenceScore,
                        BattleRequestsTableModel.BattleRequestColumn.referenceBulletDamage}));
        resultsTable.setShowGrid(true);
        resultsTableScrollPane = new JScrollPane(resultsTable);
        Utils.addTitle(resultsTableScrollPane, "Battle results");

        scoresTable = new JTable(new ScoreTableModel(challenge));
        scoresTable.setShowGrid(true);
        final JScrollPane scoresScrollPane = new JScrollPane(scoresTable);
        Utils.addTitle(scoresScrollPane, "Scores");
        scoresTable.validate();
        scoresScrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 125));
        scoresScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        infoPanel.init();
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        infoPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 65));
        infoPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 65));
        challengeResults.add(infoPanel);
        challengeResults.add(scoresScrollPane);

        final JPanel copyScoresButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        copyScoresButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        copyScoresButtons.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        final JButton copyWiki = new JButton("Copy Wiki results");
        scoresTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        copyWiki.addActionListener(new CopyWikiActionListener(challenge, scoresTable.getSelectionModel()));
        copyScoresButtons.add(copyWiki);
        challengeResults.add(copyScoresButtons);

        resultsTableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        challengeResults.add(resultsTableScrollPane);


        executorService.submit(new StateUpdater());

        setVisible(true);
        System.out.println("UI inited");
    }

    @Override
    public void battleRequestSubmitted(BattleRequest battleRequest) {
        queuePanel.battleRequestSubmitted(battleRequest);
    }

    @Override
    public void battleRequestExecutionRejected(BattleRequest battleRequest) {
        queuePanel.battleRequestExecutionRejected(battleRequest);
    }

    @Override
    public void battleRequestExecuted(BattleRequest battleRequest) {
        queuePanel.battleRequestExecuted(battleRequest);
        ((BattleRequestsTableModel) resultsTable.getModel()).addBattleRequest(battleRequest);
        getLeaf(battleRequest).addBattleRequest(battleRequest);
        ((AbstractTableModel) scoresTable.getModel()).fireTableDataChanged();
        ((TitledBorder) resultsTableScrollPane.getBorder()).setTitle(String.format("Battle results (%d, %3.2f%%)", resultsTable.getModel().getRowCount(), (double) battleRequestManager.getExecutedBattleRequests() / battleRequestManager.getTotalRequests() * 100));
        resultsTableScrollPane.repaint();
    }

    private ScoreTreeLeaf getLeaf(BattleRequest request) {
        for (AbstractScoreTreeNode node : flatNodes) {
            if (node instanceof ScoreTreeLeaf &&
                    (node.getName().equals(request.botAlias) ||
                            node.getName().equals(request.getReferenceNameAndVersion()))) {
                return (ScoreTreeLeaf) node;
            }
        }
        return null;
    }

    @Override
    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        queuePanel.battleRequestStateUpdated(battleRequest);
    }

    public void windowClosing(WindowEvent e) {
        System.out.println("Shutdown executor service");
        executorService.shutdownNow();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }


    private class StateUpdater implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    infoPanel.update();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

}
