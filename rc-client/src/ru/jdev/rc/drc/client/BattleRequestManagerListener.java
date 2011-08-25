package ru.jdev.rc.drc.client;

/**
 * User: jdev
 * Date: 25.08.11
 */
public interface BattleRequestManagerListener {

    void battleRequestSubmitted(BattleRequest battleRequest);

    void battleRequestExecutionRejected(BattleRequest battleRequest);

    void battleRequestExecuted(BattleRequest battleRequest);

    void battleRequestStateUpdated(BattleRequest battleRequest);

}
