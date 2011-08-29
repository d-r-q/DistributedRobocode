/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.util;

import javax.swing.*;

public class Utils {

    private Utils() {
    }

    public static void addTitle(JComponent cmp, String title) {
        cmp.setBorder(BorderFactory.createTitledBorder(cmp.getBorder(), title));
    }
}
