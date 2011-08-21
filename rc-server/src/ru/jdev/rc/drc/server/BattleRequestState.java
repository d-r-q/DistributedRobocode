/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

public class BattleRequestState {

    private State state = State.RECEIVED;
    private String message = "";

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum State {

        RECEIVED,
        QUEUED,
        EXECUTING,
        EXECUTED,
        REJECTED

    }

}
