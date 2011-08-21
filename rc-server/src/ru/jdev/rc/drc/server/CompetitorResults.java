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

    public int getFirsts() {
        return firsts;
    }

    public void setFirsts(int firsts) {
        this.firsts = firsts;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getBulletDamage() {
        return bulletDamage;
    }

    public void setBulletDamage(int bulletDamage) {
        this.bulletDamage = bulletDamage;
    }
}
