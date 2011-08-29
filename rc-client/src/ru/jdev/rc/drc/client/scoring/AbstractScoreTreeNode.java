/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.scoring;

public abstract class AbstractScoreTreeNode implements Score {

    protected final String name;

    public AbstractScoreTreeNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
