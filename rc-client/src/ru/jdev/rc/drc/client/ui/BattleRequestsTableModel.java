/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Vector;

public class BattleRequestsTableModel extends DefaultTableModel {

    private final Object dataLock = new Object();
    private final BattleRequestColumn[] columns;

    public BattleRequestsTableModel(BattleRequestColumn[] columns) {
        this.columns = columns;
    }

    public BattleRequestsTableModel(BattleRequestColumn[] columns, List<BattleRequest> data) {
        this.columns = columns;
        dataVector = new Vector(data);
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column].getName();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        synchronized (dataLock) {
            final BattleRequest request = (BattleRequest) dataVector.get(rowIndex);
            return columns[columnIndex].getValue(request);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void addBattleRequest(BattleRequest battleRequest) {
        synchronized (dataLock) {
            dataVector.add(battleRequest);
        }
        fireTableDataChanged();
    }

    public void battleRequestStateUpdated(BattleRequest battleRequest) {
        int idx;
        synchronized (dataLock) {
            idx = dataVector.indexOf(battleRequest);
        }
        fireTableChanged(new TableModelEvent(this, idx, idx));
    }

    public void removeBattleRequest(BattleRequest battleRequest) {
        int idx;
        synchronized (dataLock) {
            idx = dataVector.indexOf(battleRequest);
            dataVector.remove(battleRequest);
        }
        final TableModelEvent e = new TableModelEvent(this, idx);
        fireTableDataChanged();
    }

    public static class BattleRequestColumn {

        public static final BattleRequestColumn referenceBotName = new BattleRequestColumn("Reference Bot", SourceField.REFERENCE_BOT_NAME);
        public static final BattleRequestColumn requestState = new BattleRequestColumn("Request State", SourceField.REQUEST_STATE);
        public static final BattleRequestColumn challengerAps = new BattleRequestColumn("Challenger APS", SourceField.CHALLENGER_APS);
        public static final BattleRequestColumn challengerScoreGainRate = new BattleRequestColumn("Challenger Score Gain Rate", SourceField.CHALLENGER_SCORE_GAIN_RATE);
        public static final BattleRequestColumn challengerEnergyConserved = new BattleRequestColumn("Challenger Energy Conserved", SourceField.CHALLENGER_ENERGY_CONSERVED);
        public static final BattleRequestColumn challengerScore = new BattleRequestColumn("Challenger Score", SourceField.CHALLENGER_SCORE);
        public static final BattleRequestColumn challengerBulletDamage = new BattleRequestColumn("Challenger Bullet Damage", SourceField.CHALLENGER_BULLET_DAMAGE);
        public static final BattleRequestColumn referenceScore = new BattleRequestColumn("Reference Score", SourceField.REFERENCE_SCORE);
        public static final BattleRequestColumn referenceBulletDamage = new BattleRequestColumn("Reference Bullet Damage", SourceField.REFERENCE_BULLET_DAMAGE);

        private final String name;
        private final SourceField sourceField;

        private BattleRequestColumn(String name, SourceField sourceField) {
            this.name = name;
            this.sourceField = sourceField;
        }

        public String getName() {
            return name;
        }

        public String getValue(BattleRequest battleRequest) {
            switch (sourceField) {
                case REFERENCE_BOT_NAME:
                    return battleRequest.competitors.get(1).getName() + " " + battleRequest.competitors.get(1).getVersion();
                case REQUEST_STATE:
                    if (battleRequest.state == null || battleRequest.state.getMessage() == null) {
                        return "--";
                    }
                    return battleRequest.state.getMessage();
                case CHALLENGER_APS:
                    return String.format("%3.2f", battleRequest.getChallengerAPS());
                case CHALLENGER_SCORE_GAIN_RATE:
                    return String.format("%3.2f", battleRequest.getChallengerScoreGainRate());
                case CHALLENGER_ENERGY_CONSERVED:
                    return String.format("%3.2f", battleRequest.getChallengerEnergyConserved());
                case CHALLENGER_SCORE:
                    return String.valueOf(battleRequest.getChallengerScore());
                case CHALLENGER_BULLET_DAMAGE:
                    return String.valueOf(battleRequest.getChallengerBulletDamage());
                case REFERENCE_SCORE:
                    return String.valueOf(battleRequest.getReferenceScore());
                case REFERENCE_BULLET_DAMAGE:
                    return String.valueOf(battleRequest.getReferenceBulletDamage());
                default:
                    throw new IllegalArgumentException("Unsupported source field " + sourceField);
            }
        }

    }

    public enum SourceField {

        REFERENCE_BOT_NAME,
        REQUEST_STATE,
        CHALLENGER_APS,
        CHALLENGER_SCORE_GAIN_RATE,
        CHALLENGER_ENERGY_CONSERVED,
        CHALLENGER_SCORE,
        CHALLENGER_BULLET_DAMAGE,
        REFERENCE_SCORE,
        REFERENCE_BULLET_DAMAGE

    }

}
