/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import robocode.control.BattlefieldSpecification;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * User: jdev
 * Date: 13.08.11
 */
@WebService
public class RobocodeServer {

    private final Map<Integer, BattleRequest> battleRequests = new HashMap<>();

    private final CodeManager codeManager = new CodeManager();

    private BattleRequestsQueue battleRequestsQueue;
    private BattleResultsBuffer battleResultsBuffer;

    private final Object brsLock = new Object();
    private int battleRequestsSequence = 0;
    private BattleRequestQueueProcessor processor;

    public RobocodeServer() {
        // todo(zhidkov): set token
        battleRequestsQueue = new BattleRequestsQueue("token");
        battleResultsBuffer = new BattleResultsBuffer();
        final RCBattlesExecutor executor = new RCBattlesExecutor();
        processor = new BattleRequestQueueProcessor(battleRequestsQueue, executor, codeManager, battleResultsBuffer);
        Executors.newSingleThreadExecutor().execute(processor);
    }

    @WebMethod
    public void registerCode(Competitor competitor) throws IOException {
            codeManager.storeCompetitor(competitor);
    }

    @WebMethod
    public Integer executeBattle(Competitor[] competitors, BattlefieldSpecification bfSpec, int rounds, String authToken) throws IOException {
        final int battleRequestId;
        synchronized (brsLock) {
            battleRequestId = battleRequestsSequence++;
        }
        final BattleRequest battleRequest = new BattleRequest(battleRequestId, authToken, competitors, rounds, bfSpec);
        battleRequestsQueue.addBattleRequest(battleRequest);
        battleRequests.put(battleRequestId, battleRequest);
        return battleRequestId;
    }

    @WebMethod
    public BattleRequestState getState(Integer battleRequestId) {
        return battleRequests.get(battleRequestId).state;
    }

    @WebMethod
    public RSBattleResults getBattleResults(Integer battleRequestId) {
        return battleResultsBuffer.getResults(battleRequestId);
    }

    public static void main(String[] args) {
        Endpoint.publish("http://localhost:19861/RS", new RobocodeServer());
        System.out.println("Endpoint published");
    }

}
