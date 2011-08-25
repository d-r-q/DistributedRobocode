/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import javax.swing.*;
import java.awt.*;

public class InputPanel extends JPanel {

    private final JLabel label;
    private final JTextField input;

    public InputPanel(String label, int inputColumns) {
        this.label = new JLabel(label);
        this.label.setHorizontalAlignment(SwingConstants.RIGHT);
        input = new JTextField(inputColumns);

        setLayout(new FlowLayout());
        add(this.label);
        add(input);
    }

}
