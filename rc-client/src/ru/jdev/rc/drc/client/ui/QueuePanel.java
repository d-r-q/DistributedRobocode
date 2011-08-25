package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.BattleRequestManagerListener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: jdev
 * Date: 25.08.11
 */
public class QueuePanel extends JPanel implements BattleRequestManagerListener {

    private final List<BattleRequest> pendingRequests = new ArrayList<>();

    private final JTable enqueuedRequests;

    public QueuePanel(List<BattleRequest> pendingRequests) {
        this.pendingRequests.addAll(pendingRequests);
        enqueuedRequests = new JTable(new EnqueuedRequestsTableModel());
        enqueuedRequests.setShowGrid(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JScrollPane enqueuedRequestsScroll = new JScrollPane(enqueuedRequests);
        enqueuedRequests.setPreferredSize(new Dimension(Integer.MAX_VALUE, Toolkit.getDefaultToolkit().getScreenSize().height / 10));
        enqueuedRequests.setMaximumSize(new Dimension(Integer.MAX_VALUE, Toolkit.getDefaultToolkit().getScreenSize().height / 10));

        add(enqueuedRequestsScroll);
    }

    @Override
    public void battleRequestSubmitted(BattleRequest battleRequest) {
        ((EnqueuedRequestsTableModel) enqueuedRequests.getModel()).battleRequestSubmitted(battleRequest);
    }

    @Override
    public void battleRequestExecutionRejected(BattleRequest battleRequest) {
        ((EnqueuedRequestsTableModel) enqueuedRequests.getModel()).battleRequestExecutionRejected(battleRequest);
    }

    @Override
    public void battleRequestExecuted(BattleRequest battleRequest) {
        ((EnqueuedRequestsTableModel) enqueuedRequests.getModel()).battleRequestExecuted(battleRequest);
    }

    @Override
    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        ((EnqueuedRequestsTableModel) enqueuedRequests.getModel()).battleRequestStateUpdated(battleRequest);
    }

    private class EnqueuedRequestsTableModel implements TableModel, BattleRequestManagerListener {

        private final String[] columnNames = {"Refernce bot", "State"};
        private final List<BattleRequest> executingRequests = new ArrayList<>();

        private final Set<TableModelListener> listeners = new HashSet<>();

        @Override
        public int getRowCount() {
            return executingRequests.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final BattleRequest request = executingRequests.get(rowIndex);
            if (columnIndex == 0) {
                return request.competitors.get(1).getName() + request.competitors.get(1).getVersion();
            } else {
                return (request.state != null && request.state.getMessage() != null) ? request.state.getMessage() : " -- ";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

        @Override
        public void battleRequestSubmitted(BattleRequest battleRequest) {
            executingRequests.add(battleRequest);
            notifyListeners(new TableModelEvent(this, executingRequests.size() - 1));
        }

        @Override
        public void battleRequestExecutionRejected(BattleRequest battleRequest) {
            removeBattleRequest(battleRequest);
        }

        @Override
        public void battleRequestExecuted(BattleRequest battleRequest) {
            removeBattleRequest(battleRequest);
        }

        @Override
        public void battleRequestStateUpdated(BattleRequest battleRequest) {
            final int idx = executingRequests.indexOf(battleRequest);
            notifyListeners(new TableModelEvent(this, idx, 1));
        }

        private void removeBattleRequest(BattleRequest battleRequest) {
            final int idx = executingRequests.indexOf(battleRequest);
            executingRequests.remove(battleRequest);
            final TableModelEvent e = new TableModelEvent(this, idx);
            notifyListeners(e);
        }

        private void notifyListeners(TableModelEvent e) {
            for (TableModelListener l : listeners) {
                l.tableChanged(e);
            }
        }

    }

}
