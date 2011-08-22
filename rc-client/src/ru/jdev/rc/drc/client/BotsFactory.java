/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.Competitor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class BotsFactory {

    private static final File robotsDir = new File("." + File.separator + "robots");
    private final Map<String, Bot> bots = new HashMap<>();

    public Bot getBot(String name, String version) throws IOException {
        Bot bot = bots.get(name + version);

        if (bot == null) {
            byte[] botCode = getBotCode(name, version);
            bot = new Bot(name, version);
            bot.setCode(botCode);
            bot.setCompetitor(getCompetitor(name, version, botCode));
            bots.put(name + version, bot);
        }

        return bot;
    }

    public Competitor getCompetitor(String name, String version, byte[] botCode) throws IOException {
        byte[] codeCheckSum = getCodeCheckSum(botCode);

        final Competitor competitor = new Competitor();
        competitor.setCodeCheckSum(codeCheckSum);
        competitor.setName(name);
        competitor.setVersion(version);

        return competitor;
    }

    private byte[] getCodeCheckSum(byte[] competitorCode) {
        byte[] codeCheckSum;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            codeCheckSum = md.digest(competitorCode);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Can not find md5 algorithm");
        }
        return codeCheckSum;
    }

    private byte[] getBotCode(String name, String version) throws IOException {
        final File competitorJar = new File(robotsDir + File.separator + name + "_" + version + ".jar");
        byte[] competitorCode;
        try (
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final FileInputStream fis = new FileInputStream(competitorJar)
        ) {
            byte[] buffer = new byte[1024 * 200];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            competitorCode = baos.toByteArray();
        }
        return competitorCode;
    }
}
