/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import javax.swing.*;
import java.awt.*;

public class AddServerDialog extends JDialog {

    private JPanel panel = new JPanel();

    private final InputPanel ipPanel = new InputPanel("Server IP:", 10);
    private final InputPanel portPanel = new InputPanel("Server port:", 4);

    public AddServerDialog() {
        ipPanel.setBorder(BorderFactory.createEmptyBorder());
        portPanel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(ipPanel);
        panel.add(portPanel);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(panel);

        setSize(new Dimension(panel.getPreferredSize().width + 20,
                (panel.getPreferredSize().height + 20)));
    }

}
