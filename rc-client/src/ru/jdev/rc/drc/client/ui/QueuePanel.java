/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * User: jdev
 * Date: 25.08.11
 */
public class QueuePanel extends JPanel {

    private final JTable enqueuedRequests;
    private final JTable pendingRequests;
    private JScrollPane pendingRequestsScroll;

    public QueuePanel(List<BattleRequest> pendingRequests) {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        final BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        enqueuedRequests = new JTable(new BattleRequestsTableModel(
                new BattleRequestsTableModel.BattleRequestColumn[]{BattleRequestsTableModel.BattleRequestColumn.referenceBotName, BattleRequestsTableModel.BattleRequestColumn.requestState}
        ));
        enqueuedRequests.setShowGrid(true);

        JScrollPane enqueuedRequestsScroll = new JScrollPane(enqueuedRequests);
        Utils.addTitle(enqueuedRequestsScroll, "Enqueued requests");
        enqueuedRequestsScroll.setMinimumSize(new Dimension(Integer.MAX_VALUE, 150));
        enqueuedRequestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        add(enqueuedRequestsScroll);

        this.pendingRequests = new JTable(new BattleRequestsTableModel(
                new BattleRequestsTableModel.BattleRequestColumn[]{BattleRequestsTableModel.BattleRequestColumn.referenceBotName}, pendingRequests));
        this.pendingRequests.setShowGrid(true);

        pendingRequestsScroll = new JScrollPane(this.pendingRequests);
        Utils.addTitle(pendingRequestsScroll, "Pending requests (" + pendingRequests.size() + ")");
        pendingRequestsScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        pendingRequestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        add(pendingRequestsScroll);
    }

    public void battleRequestSubmitted(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).addBattleRequest(battleRequest);
        ((BattleRequestsTableModel) pendingRequests.getModel()).removeBattleRequest(battleRequest);
        final TitledBorder border = (TitledBorder) pendingRequestsScroll.getBorder();
        border.setTitle(String.format("Pending requests (%d)", pendingRequests.getModel().getRowCount()));
        validate();
        repaint();
    }

    public void battleRequestExecutionRejected(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).removeBattleRequest(battleRequest);
        ((BattleRequestsTableModel) pendingRequests.getModel()).addBattleRequest(battleRequest);
    }

    public void battleRequestExecuted(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).removeBattleRequest(battleRequest);
    }

    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        ((BattleRequestsTableModel) enqueuedRequests.getModel()).battleRequestStateUpdated(battleRequest);
    }

}
