/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

public class CompetitorResults {

    private int firsts;
    private int score;
    private int bulletDamage;

    public CompetitorResults(int firsts, int score, int bulletDamage) {
        this.firsts = firsts;
        this.score = score;
        this.bulletDamage = bulletDamage;
    }

    public CompetitorResults() {
    }
}
