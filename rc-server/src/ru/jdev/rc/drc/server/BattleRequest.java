/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

public class BattleRequest {

    public final int requestId;
    public final String secureToken;

    public final Competitor[] competitors;
    public final int rounds;
    public final BFSpec bfSpec;

    public BattleRequestState state = new BattleRequestState();

    public BattleRequest(int battleRequestId, String secureToken, Competitor[] competitors,
                         int rounds, BFSpec bfSpec) {
        requestId = battleRequestId;
        this.secureToken = secureToken;

        this.competitors = competitors;
        this.rounds = rounds;
        this.bfSpec = bfSpec;
    }

    public int getRequestId() {
        return requestId;
    }
}
