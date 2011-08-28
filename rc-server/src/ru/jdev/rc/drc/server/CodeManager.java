/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import net.sf.robocode.core.ContainerBase;
import net.sf.robocode.repository.IRepositoryManagerBase;
import sun.misc.JarFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CodeManager {

    private static final File robotsDirectory = new File("." + File.separator + "rc" + File.separator + "robots");
    private static final File repositoryDirectory = new File("." + File.separator + "rs_repository");

    private final IRepositoryManagerBase repository;

    private Method reload;

    public CodeManager() {
        // workaround for class cast exception, while casting to
        // net.sf.robocode.repository.RepositoryManager and
        // ContainerBase.getComponent(IRepositoryManager.class) returns null
        repository = ContainerBase.getComponent(IRepositoryManagerBase.class);
        try {
            reload = repository.getClass().getMethod("reload", boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

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
            fos.flush();
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
        final File competitorJarFile = getCompetitorJarFile(repositoryDirectory, competitor);
        if (!competitorJarFile.exists()) {
            throw new CompetitorNotFoundException("Competitor " + competitor + " not found", competitor);
        }

        final File targetLocation = new File(robotsDirectory.getAbsolutePath() + File.separator + competitor.name + "_" + competitor.version + ".jar");
        targetLocation.deleteOnExit();
        Utils.copyFile(competitorJarFile, targetLocation);
    }

    public void reloadRobotsDataBase() {
        try {
            reload.invoke(repository, Boolean.TRUE);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        System.out.println("Cleaning up...");
        for (File robotFile : robotsDirectory.listFiles(new JarFilter())) {
            robotFile.delete();
        }

        Utils.deleteDir(new File(robotsDirectory.getAbsoluteFile() + File.separator + ".data"));
    }
}
