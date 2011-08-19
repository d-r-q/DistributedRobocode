/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeClient {

    private void run() {
        RobocodeServerService robocodeServerService;
        try {
            robocodeServerService = new RobocodeServerService(new URL("http://localhost:19861/RS"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        final RobocodeServer server = robocodeServerService.getRobocodeServerPort();
        final CompetitorCodeFactory competitorCodeFactory = new CompetitorCodeFactory();
        final Competitor competitor;
        try {
            competitor = competitorCodeFactory.getCompetitorCode("lxx.Tomcat", "3.13.152");
            if (!server.registerCode(competitor)) {
                System.out.println("Cannot register competitor code on server!");
                return;
            }
            competitor.setCode(null);
            final BfSpec battlefieldSpecification = new BfSpec();
            battlefieldSpecification.setBfWidth(800);
            battlefieldSpecification.setBfHeight(600);
            Integer brId = server.executeBattle(Arrays.asList(competitor, competitor), battlefieldSpecification, 10, "token");
            BattleRequestState state;
            do {
                Thread.yield();
                state = server.getState(brId);
            } while (state != BattleRequestState.EXECUTED && state != BattleRequestState.REJECTED);

            System.out.println(state);
            System.out.println(server.getBattleResults(brId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RobocodeClient client = new RobocodeClient();
        client.run();
    }

}
