/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.*;
import ru.jdev.rc.drc.server.Competitor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class RCCFrame extends JFrame implements WindowListener {

    private final BattleRequestManager battleRequestManager;
    private final ProxyManager proxyManager;
    private final RobocodeClient robocodeClient;
    private final ExecutorService executorService;
    private final Challenge challenge;

    private final JPanel battleRequests = new JPanel();
    private final JPanel challengeResults = new JPanel();
    private final JPanel serversPanel = new JPanel();

    private JTable resultsTable;

    public RCCFrame(BattleRequestManager battleRequestManager, ProxyManager proxyManager, RobocodeClient robocodeClient, ExecutorService executorService, Challenge challenge) throws HeadlessException {
        this.battleRequestManager = battleRequestManager;
        this.proxyManager = proxyManager;
        this.robocodeClient = robocodeClient;
        this.executorService = executorService;
        this.challenge = challenge;
    }

    public void init() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        final JScrollPane battleRequestsScrollPane = new JScrollPane(battleRequests);
        battleRequestsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        battleRequestsScrollPane.setPreferredSize(new Dimension(270, -1));
        getContentPane().add(battleRequestsScrollPane, BorderLayout.WEST);
        getContentPane().add(challengeResults, BorderLayout.CENTER);
        getContentPane().add(serversPanel, BorderLayout.EAST);
        serversPanel.setPreferredSize(new Dimension(270, 1000));
        serversPanel.setLayout(new BoxLayout(serversPanel, BoxLayout.Y_AXIS));

        challengeResults.setLayout(new GridLayout(1, 1));
        final DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{"Reference", "Score"});
        resultsTable = new JTable(tableModel);
        resultsTable.setShowGrid(true);
        final JScrollPane resultsTableScrollPane = new JScrollPane(resultsTable);
        challengeResults.add(resultsTableScrollPane);

        battleRequests.setLayout(new BoxLayout(battleRequests, BoxLayout.Y_AXIS));
        System.out.println(battleRequests.getBackground());

        executorService.submit(new StateUpdater());

        setVisible(true);
        System.out.println("UI inited");
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private class StateUpdater implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    BattleRequestManager.State state = battleRequestManager.getState();
                    updateBattleRequests(state);
                    updateChallengeResults(state);
                    updateServersPanel(proxyManager.getState());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        private void updateBattleRequests(BattleRequestManager.State state) {
            battleRequests.removeAll();
            battleRequests.setLayout(new BoxLayout(battleRequests, BoxLayout.Y_AXIS));

            for (BattleRequest executingRequest : state.executingRequests) {
                final JPanel requestPanel = new JPanel();
                battleRequests.add(requestPanel);
                requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
                final Competitor competitor = executingRequest.competitors.get(1);
                requestPanel.add(new JLabel("Reference: " + competitor.getName() + " " + competitor.getVersion()));
                requestPanel.add(new JLabel("    " + executingRequest.state.getMessage()));
                requestPanel.setMinimumSize(new Dimension(270, 34));
                requestPanel.setMaximumSize(new Dimension(270, 34));
                requestPanel.setPreferredSize(new Dimension(270, 34));
                requestPanel.setBackground(new Color(237, 237, 194));
            }

            int idx = 1;
            for (BattleRequest pendingRequest : state.pendingRequests) {
                final JPanel requestPanel = new JPanel();
                battleRequests.add(requestPanel);
                requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
                final Competitor competitor = pendingRequest.competitors.get(1);
                requestPanel.add(new JLabel(idx + ". Reference: " + competitor.getName() + " " + competitor.getVersion()));
                requestPanel.setMinimumSize(new Dimension(270, 17));
                requestPanel.setMaximumSize(new Dimension(270, 17));
                requestPanel.setPreferredSize(new Dimension(270, 17));
                requestPanel.setBackground(new Color(194, 194, 237));

                idx++;
            }

            synchronized (battleRequests) {
                battleRequests.getParent().invalidate();
                battleRequests.validate();
                battleRequests.repaint();
            }
        }

        private void updateChallengeResults(BattleRequestManager.State state) {

            Vector<Vector<String>> rowData = new Vector<>();
            for (BattleRequest executedRequest : state.executedRequests) {
                final Competitor competitor = executedRequest.competitors.get(1);
                rowData.add(new Vector<>(Arrays.asList(competitor.getName() + " " + competitor.getVersion(), String.valueOf(executedRequest.battleResults.getCompetitorResults().get(0).getScore()))));
            }

            ((DefaultTableModel) resultsTable.getModel()).getDataVector().clear();
            ((DefaultTableModel) resultsTable.getModel()).getDataVector().addAll(rowData);

            synchronized (challengeResults) {
                ((DefaultTableModel) resultsTable.getModel()).fireTableDataChanged();
            }
        }

        private void updateServersPanel(List<ProxyManager.ProxyState> states) {
            serversPanel.removeAll();

            for (ProxyManager.ProxyState state : states) {
                System.out.println("add server panel");
                final JPanel serverPanel = new JPanel();
                serverPanel.setPreferredSize(new Dimension(270, 75));
                serverPanel.setMinimumSize(new Dimension(270, 75));
                serverPanel.setMaximumSize(new Dimension(270, 75));
                serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
                serverPanel.setBorder(BorderFactory.createTitledBorder(state.url));
                serverPanel.add(new JLabel("Bot: " + state.currentBot));
                serverPanel.add(new JLabel("State: " + state.battleRequestState));
                serversPanel.add(serverPanel);
            }

            synchronized (serversPanel) {
                serversPanel.validate();
                serversPanel.repaint();
            }
        }
    }

}
