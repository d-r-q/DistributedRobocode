/*
 * Copyright (c) 2011 Zodiac Interactive, LLC. All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.Competitor;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CompetitorCodeFactory {

    private File robotsDir = new File("." + File.separator + "robots");

    public Competitor getCompetitorCode(String name, String version) throws IOException {
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


        byte[] codeCheckSum;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            codeCheckSum = md.digest(competitorCode);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Can not find md5 algorithm");
        }


        return new Competitor(name, version, codeCheckSum, competitorCode);
    }

}
