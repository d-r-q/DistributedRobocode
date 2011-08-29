/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

import ru.jdev.rc.drc.client.BattleRequest;

public enum ScoreType {

    AVERAGED_PERCENTS_SCORE("APS"),
    SCORE_GAIN_RATE("SGR");

    private final String scoreName;

    ScoreType(String scoreName) {
        this.scoreName = scoreName;
    }

    public double getScore(BattleRequest request) {
        switch (this) {
            case AVERAGED_PERCENTS_SCORE:
                return request.getChallengerAPS();
            case SCORE_GAIN_RATE:
                return request.getChallengerScoreGainRate();
            default:
                throw new IllegalArgumentException("Unsupported scoring type: " + this);
        }
    }

    public String getScoreName() {
        return scoreName;
    }

}
