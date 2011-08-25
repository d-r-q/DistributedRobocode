/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.*;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class Utils {

    private Utils() {
    }

    public static void copyFile(File sourceLocation, File targetLocation) throws IOException {
        try (
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation)
        ) {
            byte[] buf = new byte[1024 * 1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }

}
