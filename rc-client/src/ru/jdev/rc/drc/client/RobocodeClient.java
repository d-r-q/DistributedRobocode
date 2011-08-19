/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.*;
import sun.rmi.transport.proxy.CGIHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeClient {

    private static final SimpleDateFormat executionTimeDateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final String CHALLENGES_DIR = "./challenges/";
    private static final String DISTRIBUTED_ROBOCODE_HEADER = "Distributed robocode challenge";

    private RobocodeServer serverPort;
    private CompetitorCodeFactory competitorCodeFactory;

    public RobocodeClient() {
        executionTimeDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void execute(String challenger, Challenge challenge, int seasons) {
        try {
            long startTime = System.currentTimeMillis();
            final Competitor challengerCompetitor = competitorCodeFactory.getCompetitorCode(challenger.split(" ")[0], challenger.split(" ")[1]);
            loadCompetitors(challengerCompetitor, challenge);
            challengerCompetitor.setCode(null);

            final BfSpec battlefieldSpecification = new BfSpec();
            battlefieldSpecification.setBfWidth(800);
            battlefieldSpecification.setBfHeight(600);

            for (int i = 0; i < seasons; i++) {
                for (BotsGroup botsGroup : challenge.getBotGroups()) {
                    for (Bot bot : botsGroup.getBots()) {
                        bot.getCompetitor().setCode(null);
                        Integer brId = serverPort.executeBattle(Arrays.asList(challengerCompetitor, bot.getCompetitor()), battlefieldSpecification, challenge.getRounds(), "token");
                        BattleRequestState state;
                        do {
                            Thread.yield();
                            state = serverPort.getState(brId);
                        } while (state != BattleRequestState.EXECUTED && state != BattleRequestState.REJECTED);
                        System.out.println(state);
                        System.out.println(serverPort.getBattleResults(brId));
                    }
                }
            }

            System.out.println("Challenge finished, execution time: " + executionTimeDateFormat.format(new Date(System.currentTimeMillis() - startTime)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCompetitors(Competitor challengerBot, Challenge challenge) throws IOException {
        registerCompetitor(challengerBot);

        for (BotsGroup botGroup : challenge.getBotGroups()) {
            for (Bot bot : botGroup.getBots()) {
                final Competitor competitorObj = competitorCodeFactory.getCompetitorCode(bot.getBotName(), bot.getBotVersion());
                bot.setCompetitor(competitorObj);
                registerCompetitor(competitorObj);
            }
        }
    }

    private void registerCompetitor(Competitor competitorObj) {
        final byte[] code = competitorObj.getCode();
        competitorObj.setCode(null);
        if (!serverPort.hasCompetitor(competitorObj)) {
            System.out.printf("Registering competitor %s %s\n", competitorObj.getName(), competitorObj.getVersion());
            competitorObj.setCode(code);
            serverPort.registerCode(competitorObj);
        }
    }

    private boolean init() throws MalformedURLException {
        RobocodeServerService robocodeServerService = new RobocodeServerService(new URL("http://localhost:19861/RS"));
        serverPort = robocodeServerService.getRobocodeServerPort();
        competitorCodeFactory = new CompetitorCodeFactory();
        return false;
    }

    private Challenge parseChallenge(String file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(CHALLENGES_DIR + file));
        reader.mark(256);
        final String firstLine = reader.readLine();
        if (firstLine.equals(DISTRIBUTED_ROBOCODE_HEADER)) {
            // todo: handle new format
            return null;
        } else {
            // handle RoboResearch challenge
            reader.reset();
            return Challenge.load(reader);
        }
    }

    public static void main(String[] args) throws IOException {
        RobocodeClient client = new RobocodeClient();
        client.init();
        client.execute(args[0], client.parseChallenge(args[1]), Integer.parseInt(args[2]));
    }

}
