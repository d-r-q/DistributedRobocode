/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: jdev
 * Date: 19.08.11
 */
public class Challenge {

    private final List<BotsGroup> botsGroups = new ArrayList<>();
    private final Set<Bot> allBots = new HashSet<>();

    private Bot challenger;

    private String name;
    private String scoringType;
    private int rounds;
    private int seasons;

    public int getRounds() {
        return rounds;
    }

    private void addGroup(BotsGroup group) {
        botsGroups.add(group);
        for (Bot bot : group.getBots()) {
            allBots.add(bot);
        }
    }

    public List<BotsGroup> getBotGroups() {
        return botsGroups;
    }

    public Bot getChallenger() {
        return challenger;
    }

    public void setChallenger(Bot challenger) {
        allBots.add(challenger);
        this.challenger = challenger;
    }

    public Set<Bot> getAllBots() {
        return allBots;
    }

    // original code from RoboResearch (http://robowiki.net/wiki/RoboResearch)
    public static Challenge load(Reader inStream, BotsFactory botsFactory) throws IOException {

        // input stream must be closed by caller
        final LineNumberReader in = new LineNumberReader(inStream);
        final Challenge c = new Challenge();
        // the first line in the name
        c.name = in.readLine().trim();

        // the second line's first word is the scoring type
        c.scoringType = in.readLine().split(" ")[0];

        // the third line's first word is the default number of rounds
        c.rounds = Integer.parseInt(in.readLine().split(" ")[0]);

        String line;
        while (true) {

            // read the next line, looking for a category name
            line = in.readLine();
            if (line == null) {
                return c;
            } else if ((line = line.trim()).length() == 0) {
                continue;
            } else if (!line.endsWith("{")) { // grab the category name
                throw new IOException(
                        in.getLineNumber()
                                + ": Bad file format, categories names must be followed by a '{' on the same line");
            }

            final String category = line.substring(0, line.length() - 1).trim();
            if (category.length() == 0) {
                throw new IOException(
                        in.getLineNumber()
                                + ": Bad file format, categories names must be followed by a '{' on the same line");
            }

            final BotsGroup group = new BotsGroup(category);
            // fill the category with each challenger
            while (true) {

                // read the next line
                line = in.readLine();
                if (line == null) {
                    throw new IOException(
                            in.getLineNumber()
                                    + ": Bad file format, categories must be terminated by a '}' on its own line");
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                // stop if it's the end of the category
                if (line.equals("}")) {
                    break;
                }

                // add the line as a bot to the category!
                final String[] tokens = line.split(",");
                final String[] botName = tokens[0].split(" ");
                Bot bot = botsFactory.getBot(botName[0], botName[1]);
                if (tokens.length > 1) {
                    bot.setAlias(tokens[1]);
                }
                group.addBot(bot);
            }
            c.addGroup(group);
        }
    }

}