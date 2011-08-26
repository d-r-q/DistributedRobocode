/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.*;
import ru.jdev.rc.drc.client.proxy.ProxyList;
import ru.jdev.rc.drc.client.proxy.RobocodeServerProxy;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;

public class RCCFrame extends JFrame implements BattleRequestManagerListener {

    private final BattleRequestManager battleRequestManager;
    private final ProxyList proxyList;
    private final RobocodeClient robocodeClient;
    private final ExecutorService executorService;
    private final Challenge challenge;

    private final JPanel challengeResults = new JPanel();
    private final JPanel serversPanel = new JPanel();
    private final JPanel infoPanel = new JPanel();

    private JTable resultsTable;
    private QueuePanel queuePanel;

    public RCCFrame(BattleRequestManager battleRequestManager, ProxyList proxyList, RobocodeClient robocodeClient, ExecutorService executorService, Challenge challenge) throws HeadlessException {
        this.battleRequestManager = battleRequestManager;
        this.proxyList = proxyList;
        this.robocodeClient = robocodeClient;
        this.executorService = executorService;
        this.challenge = challenge;
    }

    public void init() {
        battleRequestManager.addListener(this);

        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        queuePanel = new QueuePanel(battleRequestManager.getPendingRequests());
        queuePanel.setPreferredSize(new Dimension(370, Integer.MAX_VALUE));
        getContentPane().add(queuePanel, BorderLayout.WEST);
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

        challengeResults.setLayout(new BoxLayout(challengeResults, BoxLayout.Y_AXIS));
        resultsTable = new JTable(new BattleRequestsTableModel(new BattleRequestsTableModel.BattleRequestColumn[]
                {BattleRequestsTableModel.BattleRequestColumn.referenceBotName,
                        BattleRequestsTableModel.BattleRequestColumn.challengerAps,
                        BattleRequestsTableModel.BattleRequestColumn.challengerScore,
                        BattleRequestsTableModel.BattleRequestColumn.challengerBulletDamage,
                        BattleRequestsTableModel.BattleRequestColumn.referenceScore,
                        BattleRequestsTableModel.BattleRequestColumn.referenceBulletDamage}));
        resultsTable.setShowGrid(true);
        final JScrollPane resultsTableScrollPane = new JScrollPane(resultsTable);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        infoPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 100));
        infoPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 100));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));
        challengeResults.add(infoPanel, BorderLayout.NORTH);
        resultsTableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        challengeResults.add(resultsTableScrollPane, BorderLayout.CENTER);

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
    }

    @Override
    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        queuePanel.battleRequestStateUpdated(battleRequest);
    }

    private class StateUpdater implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    infoPanel.removeAll();

                    infoPanel.add(new JLabel("Challenger: " + challenge.getChallenger().getBotName() + " " + challenge.getChallenger().getBotVersion()));
                    infoPanel.add(new JLabel(String.format("APS: %3.2f", battleRequestManager.getAps())));
                    if (robocodeClient.getFinishTime() == -1) {
                        infoPanel.add(new JLabel("Elapsed time: " + RobocodeClient.executionTimeDateFormat.format(new Date(System.currentTimeMillis() - robocodeClient.getStartTime()))));
                    } else {
                        infoPanel.add(new JLabel("Execution time: " + RobocodeClient.executionTimeDateFormat.format(new Date(robocodeClient.getFinishTime() - robocodeClient.getStartTime()))));
                    }
                    infoPanel.add(new JLabel("Estimated remaining time: " + RobocodeClient.executionTimeDateFormat.format(new Date(robocodeClient.getEstimatedRemainingTime()))));

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
    }

}
