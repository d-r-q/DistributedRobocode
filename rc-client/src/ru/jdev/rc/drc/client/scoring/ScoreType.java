/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import ru.jdev.rc.drc.client.BattleRequest;

public enum ScoreType {

    PERCENT_SCORE("APS"),
    SCORE_GAIN_RATE("SGR"),
    AVERAGE_BULLET_DAMAGE("Chr blt dmg"),
    AVERAGE_ENERGY_CONSERVED("Chr avg enrg cons");

    private final String scoreName;

    ScoreType(String scoreName) {
        this.scoreName = scoreName;
    }

    public double getScore(BattleRequest request) {
        switch (this) {
            case PERCENT_SCORE:
                return request.getChallengerAPS();
            case SCORE_GAIN_RATE:
                return request.getChallengerScoreGainRate();
            case AVERAGE_BULLET_DAMAGE:
                return request.getChallengerBulletDamage() / request.rounds;
            case AVERAGE_ENERGY_CONSERVED:
                return request.getChallengerEnergyConserved();
            default:
                throw new IllegalArgumentException("Unsupported scoring type: " + this);
        }
    }

    public String getScoreName() {
        return scoreName;
    }

}
