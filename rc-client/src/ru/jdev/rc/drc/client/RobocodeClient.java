/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.client.proxy.ProxyList;
import ru.jdev.rc.drc.client.scoring.ScoreType;
import ru.jdev.rc.drc.client.ui.CopyWikiActionListener;
import ru.jdev.rc.drc.client.ui.RCCFrame;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeClient {

    public static final SimpleDateFormat executionTimeDateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final String CHALLENGES_DIR = "./challenges/";
    private static final String DISTRIBUTED_ROBOCODE_HEADER = "Distributed robocode challenge";

    private final BattleRequestManager battleRequestManager;
    private final ProxyList proxyList;
    private final Challenge challenge;

    private long battlesExecutionStartTime = -1;
    private long startTime;
    private long finishTime = -1;

    public RobocodeClient(BattleRequestManager battleRequestManager, ProxyList proxyList, Challenge challenge) throws IOException {
        this.battleRequestManager = battleRequestManager;
        this.proxyList = proxyList;
        this.challenge = challenge;

        executionTimeDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void run() {
        startTime = System.currentTimeMillis();

        proxyList.connectAllProxies();
        while (battleRequestManager.hasNotExecutedRequests()) {
            if (battleRequestManager.getRemainingBattleRequests() < battleRequestManager.getTotalRequests() &&
                    battlesExecutionStartTime == -1) {
                battlesExecutionStartTime = System.currentTimeMillis();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            System.out.println("Estimated remaining time: " + executionTimeDateFormat.format(new Date(getEstimatedRemainingTime())));
        }
        finishTime = System.currentTimeMillis();

        System.out.println("Challenge finished, execution time: " + executionTimeDateFormat.format(new Date(System.currentTimeMillis() - startTime)));
        System.out.println(CopyWikiActionListener.getWikiStr(challenge, ScoreType.valueOf(challenge.getScoringType())));
    }

    public long getEstimatedRemainingTime() {
        final int executedBattles = battleRequestManager.getExecutedBattleRequests();
        if (executedBattles == 0) {
            return 0;
        }
        final long millisPerBattle = (System.currentTimeMillis() - battlesExecutionStartTime) / executedBattles;

        return battleRequestManager.getRemainingBattleRequests() * millisPerBattle;
    }

    private static Challenge parseChallenge(String challenger, String file, int seasons, BotsFactory botsFactory) throws IOException {
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
            challenge.setSeasons(seasons);
            challenge.setChallenger(botsFactory.getBot(challenger.split(" ")[0], challenger.split(" ")[1]));
            return challenge;
        }
    }

    public static void main(String[] args) throws IOException {
        boolean runUI = false;
        final List<String> argsList = new ArrayList<>(Arrays.asList(args));
        for (Iterator<String> argsIter = argsList.iterator(); argsIter.hasNext(); ) {
            if (argsIter.next().equals("-ui")) {
                runUI = true;
                argsIter.remove();
            }
        }
        final ExecutorService executorService = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdownExecutor(executorService);
            }
        });
        final Challenge challenge = parseChallenge(argsList.get(0), argsList.get(1), Integer.parseInt(argsList.get(2)), new BotsFactory());
        final BattleRequestManager battleRequestManager = new BattleRequestManager(challenge);
        final RobocodeClient client = new RobocodeClient(battleRequestManager, new ProxyList(executorService, challenge.getAllBots(), battleRequestManager), challenge);
        if (runUI) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            new RCCFrame(client.battleRequestManager, client.proxyList, client, executorService, challenge).init();
        }
        client.run();

        if (!runUI) {
            shutdownExecutor(executorService);
        }
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }
}
