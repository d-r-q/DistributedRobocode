/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.Serializable;

public class RSBattleResults implements Serializable {

    public CompetitorResults[] competitorResults;

    public long requestId;

    public RSBattleResults() {
    }

    public RSBattleResults(CompetitorResults[] compRess) {
        competitorResults = compRess;
    }
}
