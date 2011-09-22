/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

public interface Score {

    double getAvgScore(ScoreType scoreType);

    double getMedScore(ScoreType scoreType);

}
