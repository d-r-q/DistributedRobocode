/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.Serializable;

public class Competitor implements Serializable {

    public String name;
    public String version;
    public byte[] codeCheckSum;

    public Competitor() {
    }

    @Override
    public String toString() {
        return name + " " + version + "(" + new String(codeCheckSum) + ")";
    }
}
