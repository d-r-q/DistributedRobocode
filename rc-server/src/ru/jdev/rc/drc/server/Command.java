/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

public class Command {

    public final Client client;
    public final BattleRequest battleRequest;

    public Command(Client client, BattleRequest battleRequest) {
        this.client = client;
        this.battleRequest = battleRequest;
    }
}
