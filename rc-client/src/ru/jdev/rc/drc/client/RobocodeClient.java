/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private final BotsFactory botsFactory = new BotsFactory();
    private final ExecutorService service = Executors.newCachedThreadPool();

    public RobocodeClient() throws IOException {
        executionTimeDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void run(Challenge challenge, int seasons) {
        try {
            long startTime = System.currentTimeMillis();
            final ProxyManager proxyManager = new ProxyManager(service, challenge.getAllBots());
            final BattleRequestManager battleRequestManager = new BattleRequestManager(challenge, seasons);

            BattleRequest requestToExecute;
            while (battleRequestManager.hasNotExecutedRequests()) {
                final RobocodeServerProxy freeProxy = proxyManager.getFreeProxy();
                checkProxies(proxyManager, battleRequestManager);
                requestToExecute = battleRequestManager.getBattleRequest();
                if (requestToExecute == null) {
                    break;
                }
                System.out.println("Free proxy: " + freeProxy + ", enqueue request " + requestToExecute.localId);

                freeProxy.enqueueBattle(requestToExecute);

                System.out.println(battleRequestManager);
            }

            System.out.println("Challenge finished, execution time: " + executionTimeDateFormat.format(new Date(System.currentTimeMillis() - startTime)));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            service.shutdownNow();
        }
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

    private Challenge parseChallenge(String challenger, String file, BotsFactory botsFactory) throws IOException {
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
        RobocodeClient client = new RobocodeClient();
        client.run(client.parseChallenge(args[0], args[1], client.botsFactory), Integer.parseInt(args[2]));
    }

}
