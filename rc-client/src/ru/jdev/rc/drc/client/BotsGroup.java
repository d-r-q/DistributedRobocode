/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 19.08.11
 */
public class BotsGroup {

    private final List<Bot> bots = new ArrayList<>();

    private final String name;

    public BotsGroup(String name) {
        this.name = name;
    }

    public void addBot(Bot bot) {
        bots.add(bot);
    }

    public List<Bot> getBots() {
        return bots;
    }

    public String getName() {
        return name;
    }
}
