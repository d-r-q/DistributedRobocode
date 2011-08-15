/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.IOException;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class CompetitorNotFoundException extends IOException {

    private final Competitor competitor;

    public CompetitorNotFoundException(String message, Competitor competitor) {
        super(message);
        this.competitor = competitor;
    }
}
