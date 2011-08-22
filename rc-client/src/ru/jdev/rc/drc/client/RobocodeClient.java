/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.client.ui.RCCFrame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeClient {

    private static final SimpleDateFormat executionTimeDateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final String CHALLENGES_DIR = "./challenges/";
    private static final String DISTRIBUTED_ROBOCODE_HEADER = "Distributed robocode challenge";

    private final BattleRequestManager battleRequestManager;
    private final ProxyManager proxyManager;
    private final ExecutorService service;

    private long battlesExecutionStartTime = -1;

    public RobocodeClient(BattleRequestManager battleRequestManager, ProxyManager proxyManager, ExecutorService service) throws IOException {
        this.battleRequestManager = battleRequestManager;
        this.proxyManager = proxyManager;
        this.service = service;

        executionTimeDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void run() {
        long startTime = System.currentTimeMillis();

        BattleRequest requestToExecute;
        while (battleRequestManager.hasNotExecutedRequests()) {
            final RobocodeServerProxy freeProxy = proxyManager.getFreeProxy();
            checkProxies(proxyManager, battleRequestManager);
            requestToExecute = battleRequestManager.getBattleRequest();
            if (requestToExecute == null) {
                break;
            }
            System.out.println("Free proxy: " + freeProxy + ", enqueue request " + requestToExecute.localId);

            if (battlesExecutionStartTime == -1) {
                battlesExecutionStartTime = System.currentTimeMillis();
            }
            freeProxy.enqueueBattle(requestToExecute);

            System.out.println(battleRequestManager);
            System.out.println("Estimated remaining time: " + executionTimeDateFormat.format(new Date(getEstimatedRemainingTime())));
        }

        System.out.println("Challenge finished, execution time: " + executionTimeDateFormat.format(new Date(System.currentTimeMillis() - startTime)));
    }

    public long getEstimatedRemainingTime() {
        final int executedBattles = battleRequestManager.getExecutedBattleRequests();
        if (executedBattles == 0) {
            return 0;
        }
        final long millisPerBattle = (System.currentTimeMillis() - battlesExecutionStartTime) / executedBattles;

        return battleRequestManager.getRemainingBattleRequests() * millisPerBattle;
    }

    private void checkProxies(ProxyManager proxyManager, BattleRequestManager battleRequestManager) {
        System.out.println("Checking proxies...");
        for (RobocodeServerProxy proxy : proxyManager.getAvailableProxies()) {
            if (proxy.hasResults()) {
                System.out.println("Proxy " + proxy + " ready");
                for (BattleRequest executedRequest : proxy.flushExecutedBattleRequestsBuffer()) {
                    battleRequestManager.battleRequestExecuted(executedRequest);
                    System.out.println(executedRequest.localId + ": " + executedRequest.battleResults.getCompetitorResults().get(0).getScore());
                }
            }
        }
        System.out.println("Proxies checked...");
    }

    private static Challenge parseChallenge(String challenger, String file, BotsFactory botsFactory) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(CHALLENGES_DIR + file));
        reader.mark(256);
        final String firstLine = reader.readLine();
        if (firstLine.equals(DISTRIBUTED_ROBOCODE_HEADER)) {
            // todo: handle new format
            return null;
        } else {
            // handle RoboResearch challenge
            reader.reset();
            final Challenge challenge = Challenge.load(reader, botsFactory);
            challenge.setChallenger(botsFactory.getBot(challenger.split(" ")[0], challenger.split(" ")[1]));
            return challenge;
        }
    }

    public static void main(String[] args) throws IOException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Challenge challenge = parseChallenge(args[0], args[1], new BotsFactory());
        final RobocodeClient client = new RobocodeClient(new BattleRequestManager(challenge, Integer.parseInt(args[2])), new ProxyManager(executorService, challenge.getAllBots()), executorService);
        new RCCFrame(client.battleRequestManager, client.proxyManager, client, executorService, challenge).init();
        client.run();
    }

}
