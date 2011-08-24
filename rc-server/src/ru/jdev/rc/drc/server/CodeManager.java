/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import net.sf.robocode.core.ContainerBase;
import net.sf.robocode.repository.IRepositoryManagerBase;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CodeManager {

    private static final File devPathDirectory = new File("." + File.separator + "rc" + File.separator + "dev_path");
    private static final File robotsDirectory = new File("." + File.separator + "rc" + File.separator + "robots");
    private static final File repositoryDirectory = new File("." + File.separator + "rs_repository");

    private final Map<String, byte[]> loadedCode = new HashMap<>();

    public void storeCompetitor(Competitor competitor, byte[] code) throws java.io.IOException {
        final File competitorJarFile = getCompetitorJarFile(repositoryDirectory, competitor);
        if (competitorJarFile.exists()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        try (
                final FileOutputStream fos = new FileOutputStream(competitorJarFile)
        ) {
            fos.write(code);
        }

        System.out.println("Store time: " + (System.currentTimeMillis() - startTime));
    }

    public boolean hasCompetitor(Competitor competitor) {
        final File competitorCodeDir = getCompetitorJarFile(repositoryDirectory, competitor);
        return competitorCodeDir.exists();
    }

    private File getCompetitorJarFile(File root, Competitor competitor) {
        StringBuffer checkSum = new StringBuffer();
        for (Byte b : competitor.codeCheckSum) {
            checkSum.append(Integer.toHexString(0xFF & b));
        }
        return new File(root + File.separator + new String(checkSum));
    }

    public void loadCompetitor(Competitor competitor) throws java.io.IOException {
        final byte[] loadedVersion = loadedCode.get(competitor.name + competitor.version);
        if (loadedVersion != null && Arrays.equals(loadedVersion, competitor.codeCheckSum)) {
            return;
        }

        final File competitorJarFile = getCompetitorJarFile(repositoryDirectory, competitor);
        if (!competitorJarFile.exists()) {
            throw new CompetitorNotFoundException("Competitor " + competitor + " not found", competitor);
        }

        Utils.copyFile(competitorJarFile, new File(robotsDirectory.getAbsolutePath() + File.separator + competitor.name + "_" + competitor.version + ".jar"));
        loadedCode.put(competitor.name + competitor.version, competitor.codeCheckSum);

        reloadRobotsDataBase();
    }

    public void reloadRobotsDataBase() {
        final IRepositoryManagerBase repository = ContainerBase.getComponent(IRepositoryManagerBase.class);
        try {
            repository.getClass().getMethod("reload", boolean.class).invoke(repository, Boolean.TRUE);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
