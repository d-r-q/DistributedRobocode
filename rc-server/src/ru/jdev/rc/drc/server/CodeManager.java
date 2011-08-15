/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class CodeManager {

    private final File robotsDirectory = new File("." + File.separator + "rc" + File.separator + "dev_path");
    private final File repositoryDirectory = new File("." + File.separator + "rs_repository");

    public void storeCompetitor(Competitor competitor) throws java.io.IOException {
        final File competitorCodeDir = getCompetitorDir(repositoryDirectory, competitor);
        if (competitorCodeDir.exists()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        final JarInputStream jis = new JarInputStream(new ByteArrayInputStream(competitor.code));
        JarEntry e;
        while ((e = jis.getNextJarEntry()) != null) {
            String eName = e.getName();
            int idx = eName.lastIndexOf("/");

            final File dir = new File(competitorCodeDir.getAbsolutePath() + File.separator + eName.substring(0, idx));
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new java.io.IOException("Cannot create dir " + dir.getCanonicalPath());
                }
            }

            if (idx != eName.length() - 1) {
                final FileOutputStream fos = new FileOutputStream(new File(dir.getAbsolutePath() + File.separator + eName.substring(idx + 1)));
                byte[] buff = new byte[1024];
                int len;
                while ((len = jis.read(buff)) != -1) {
                    fos.write(buff, 0, len);
                }
                fos.flush();
                fos.close();
            }
        }
        jis.close();

        System.out.println("Store time: " + (System.currentTimeMillis() - startTime));
    }

    private File getCompetitorDir(File root, Competitor competitor) {
        StringBuffer checkSum = new StringBuffer();
        for (Byte b : competitor.codeCheckSum) {
            checkSum.append(Integer.toHexString(0xFF & b));
        }
        return new File(root + File.separator + competitor.name + "_" + competitor.version + File.separator + new String(checkSum));
    }

    public void loadCompetitor(Competitor competitor) throws java.io.IOException {
        final File competitorDir = getCompetitorDir(repositoryDirectory, competitor);
        if (!competitorDir.exists()) {
            throw new CompetitorNotFoundException("Competitor " + competitor + " not found", competitor);
        }

        Utils.copyDirectory(competitorDir, robotsDirectory);
    }
}
