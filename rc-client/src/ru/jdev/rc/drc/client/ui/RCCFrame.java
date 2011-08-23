/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.BattleRequestManager;
import ru.jdev.rc.drc.client.Challenge;
import ru.jdev.rc.drc.client.RobocodeClient;
import ru.jdev.rc.drc.client.proxy.ProxyList;
import ru.jdev.rc.drc.client.proxy.RobocodeServerProxy;
import ru.jdev.rc.drc.server.Competitor;
import ru.jdev.rc.drc.server.CompetitorResults;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class RCCFrame extends JFrame implements WindowListener {

    private final BattleRequestManager battleRequestManager;
    private final ProxyList proxyList;
    private final RobocodeClient robocodeClient;
    private final ExecutorService executorService;
    private final Challenge challenge;

    private final JPanel battleRequests = new JPanel();
    private final JPanel challengeResults = new JPanel();
    private final JPanel serversPanel = new JPanel();
    private final JPanel infoPanel = new JPanel();

    private JTable resultsTable;

    public RCCFrame(BattleRequestManager battleRequestManager, ProxyList proxyList, RobocodeClient robocodeClient, ExecutorService executorService, Challenge challenge) throws HeadlessException {
        this.battleRequestManager = battleRequestManager;
        this.proxyList = proxyList;
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

        for (RobocodeServerProxy proxy : proxyList.getAvailableProxies()) {
            final ServerPanel serverPanel = new ServerPanel(proxy);
            serverPanel.init();
            proxy.addListener(serverPanel);
            serversPanel.add(serverPanel);
        }

        challengeResults.setLayout(new GridLayout(2, 1));
        final DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{"Reference", "Challenger APS", "Challenger score", "Challenger bullet damage", "Refernce score", "Refernce bullet damage"});
        resultsTable = new JTable(tableModel);
        resultsTable.setShowGrid(true);
        final JScrollPane resultsTableScrollPane = new JScrollPane(resultsTable);
        challengeResults.add(infoPanel, BorderLayout.NORTH);
        challengeResults.add(resultsTableScrollPane, BorderLayout.CENTER);

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
                    infoPanel.removeAll();
                    infoPanel.setLayout(new GridLayout(4, 1));
                    infoPanel.setPreferredSize(new Dimension(200, 200));
                    infoPanel.setMinimumSize(new Dimension(200, 200));
                    infoPanel.setMaximumSize(new Dimension(200, 200));

                    JPanel challengerNamePanel = new JPanel();
                    challengerNamePanel.setMinimumSize(new Dimension(100, 100));
                    challengerNamePanel.setLayout(new BoxLayout(challengerNamePanel, BoxLayout.X_AXIS));
                    infoPanel.add(challengerNamePanel);
                    challengerNamePanel.add(new JLabel("Challenger: " + challenge.getChallenger().getBotName() + " " + challenge.getChallenger().getBotVersion()));

                    JPanel apsPanel = new JPanel();
                    apsPanel.setMinimumSize(new Dimension(100, 100));
                    apsPanel.setLayout(new BoxLayout(apsPanel, BoxLayout.X_AXIS));
                    infoPanel.add(apsPanel);
                    apsPanel.add(new JLabel(String.format("APS: %3.2f", battleRequestManager.getAps())));

                    JPanel elapsedTimePanel = new JPanel();
                    elapsedTimePanel.setMinimumSize(new Dimension(100, 100));
                    elapsedTimePanel.setLayout(new BoxLayout(elapsedTimePanel, BoxLayout.X_AXIS));
                    infoPanel.add(elapsedTimePanel);
                    if (robocodeClient.getFinishTime() == -1) {
                        elapsedTimePanel.add(new JLabel("Elapsed time: " + RobocodeClient.executionTimeDateFormat.format(new Date(System.currentTimeMillis() - robocodeClient.getStartTime()))));
                    } else {
                        elapsedTimePanel.add(new JLabel("Execution time: " + RobocodeClient.executionTimeDateFormat.format(new Date(robocodeClient.getFinishTime() - robocodeClient.getStartTime()))));
                    }

                    JPanel remainingTimePanel = new JPanel();
                    remainingTimePanel.setMinimumSize(new Dimension(100, 100));
                    remainingTimePanel.setLayout(new BoxLayout(remainingTimePanel, BoxLayout.X_AXIS));
                    infoPanel.add(remainingTimePanel);
                    remainingTimePanel.add(new JLabel("Estimated remaining time: " + RobocodeClient.executionTimeDateFormat.format(new Date(robocodeClient.getEstimatedRemainingTime()))));

                    infoPanel.validate();
                    infoPanel.repaint();
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
                if (executingRequest.state != null) {
                    requestPanel.add(new JLabel("    " + executingRequest.state.getMessage()));
                }
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
                final CompetitorResults cr = executedRequest.battleResults.getCompetitorResults().get(0);
                final CompetitorResults rr = executedRequest.battleResults.getCompetitorResults().get(1);
                rowData.add(new Vector<>(Arrays.asList(competitor.getName() + " " + competitor.getVersion(),
                        String.format("%3.2f", ((double) cr.getScore() / (double) (cr.getScore() + rr.getScore())) * 100),
                        String.valueOf(cr.getScore()),
                        String.valueOf(cr.getBulletDamage()),
                        String.valueOf(rr.getScore()),
                        String.valueOf(rr.getBulletDamage()))));
            }

            ((DefaultTableModel) resultsTable.getModel()).getDataVector().clear();
            ((DefaultTableModel) resultsTable.getModel()).getDataVector().addAll(rowData);

            synchronized (challengeResults) {
                ((DefaultTableModel) resultsTable.getModel()).fireTableDataChanged();
            }
        }
    }

}
