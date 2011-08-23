/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.BattleRequestState;
import ru.jdev.rc.drc.server.BfSpec;
import ru.jdev.rc.drc.server.Competitor;
import ru.jdev.rc.drc.server.RsBattleResults;

import java.util.List;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class BattleRequest {

    public final List<Competitor> competitors;
    public final BfSpec bfSpec;
    public final int rounds;

    public int localId;
    public int remoteId;
    public RsBattleResults battleResults;
    public BattleRequestState state;
    public long requestStartExecutingTime;
    public int currentRound;

    public BattleRequest(List<Competitor> competitors, BfSpec bfSpec, int rounds) {
        this.competitors = competitors;
        this.bfSpec = bfSpec;
        this.rounds = rounds;
    }

    @Override
    public String toString() {
        return String.valueOf(localId);
    }
}
