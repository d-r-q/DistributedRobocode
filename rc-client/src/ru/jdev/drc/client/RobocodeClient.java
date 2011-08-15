/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.*;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeClient {

    private void run() {
        RobocodeServerService robocodeServerService = null;
        try {
            robocodeServerService = new RobocodeServerService(new URL("http://localhost:19861/RS"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        final RobocodeServer server = robocodeServerService.getRobocodeServerPort();
        final CompetitorCodeFactory competitorCodeFactory = new CompetitorCodeFactory();
        try {
            final Competitor competitor = competitorCodeFactory.getCompetitorCode("lxx.Tomcat", "3.13.152");
            server.registerCode(competitor);
            competitor.code = null;
            Integer brId = server.executeBattle(new Competitor[]{competitor, competitor}, new BattlefieldSpecification(800, 600), 10, "token");
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
