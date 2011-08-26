/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.BattleRequestManagerListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: jdev
 * Date: 25.08.11
 */
public class QueuePanel extends JPanel implements BattleRequestManagerListener {

    private final JTable enqueuedRequests;
    private final JTable pendingRequests;

    public QueuePanel(List<BattleRequest> pendingRequests) {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        final BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        enqueuedRequests = new JTable(new BattleRequestsTableModel(
                new BattleRequestsTableModel.BattleRequestColumn[]{BattleRequestsTableModel.BattleRequestColumn.referenceBotName, BattleRequestsTableModel.BattleRequestColumn.requestState}
        ));
        enqueuedRequests.setShowGrid(true);

        JScrollPane enqueuedRequestsScroll = new JScrollPane(enqueuedRequests);
        enqueuedRequestsScroll.setBorder(BorderFactory.createTitledBorder(enqueuedRequestsScroll.getBorder(), "Enqueued requests"));
        enqueuedRequestsScroll.setMinimumSize(new Dimension(Integer.MAX_VALUE, 150));
        enqueuedRequestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        add(enqueuedRequestsScroll);

        this.pendingRequests = new JTable(new BattleRequestsTableModel(
                new BattleRequestsTableModel.BattleRequestColumn[]{BattleRequestsTableModel.BattleRequestColumn.referenceBotName}, pendingRequests));
        this.pendingRequests.setShowGrid(true);

        JScrollPane pendingRequestsScroll = new JScrollPane(this.pendingRequests);
        pendingRequestsScroll.setBorder(BorderFactory.createTitledBorder(pendingRequestsScroll.getBorder(), "Pending requests"));
        pendingRequestsScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        pendingRequestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        add(pendingRequestsScroll);
    }

    @Override
    public void battleRequestSubmitted(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).addBattleRequest(battleRequest);
        ((BattleRequestsTableModel) pendingRequests.getModel()).removeBattleRequest(battleRequest);
    }

    @Override
    public void battleRequestExecutionRejected(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).removeBattleRequest(battleRequest);
        ((BattleRequestsTableModel) pendingRequests.getModel()).addBattleRequest(battleRequest);
    }

    @Override
    public void battleRequestExecuted(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).removeBattleRequest(battleRequest);
    }

    @Override
    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).battleRequestStateUpdated(battleRequest);
    }

}
