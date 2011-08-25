/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.BattleRequest;
import ru.jdev.rc.drc.client.proxy.ProxyListener;
import ru.jdev.rc.drc.client.proxy.RobocodeServerProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerPanel extends JPanel implements ProxyListener, ActionListener {

    private final JLabel currentBotLabel = new JLabel("Bot: --");
    private final JLabel requestStateLabel = new JLabel("State: --");
    private final JLabel serverStateLabel = new JLabel("Server: --");
    private final JButton reconnect = new JButton("Reconnect");

    private final RobocodeServerProxy proxy;

    public ServerPanel(RobocodeServerProxy proxy) {
        this.proxy = proxy;
    }

    public void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder(proxy.getUrl()));

        add(currentBotLabel);
        add(requestStateLabel);
        add(serverStateLabel);
        add(reconnect);
        reconnect.setEnabled(false);
        reconnect.addActionListener(this);

        this.setPreferredSize(new Dimension(270, getPreferredSize().height));
        this.setMinimumSize(new Dimension(270, getPreferredSize().height));
        this.setMaximumSize(new Dimension(270, getPreferredSize().height));
    }

    public void proxyStateUpdate() {
        if (proxy.isConnected()) {
            setBackground(new Color(194, 237, 194));
        } else {
            setBackground(new Color(237, 194, 194));
        }
        reconnect.setEnabled(!proxy.isConnected());

        final BattleRequest currentRequest = proxy.getCurrentRequest();
        if (currentRequest != null) {
            currentBotLabel.setText(String.format("Bot: %s %s", currentRequest.competitors.get(1).getName(), currentRequest.competitors.get(1).getVersion()));
            requestStateLabel.setText(String.format("State: %s", currentRequest.state.getMessage()));
        } else {
            currentBotLabel.setText("Bot: --");
            requestStateLabel.setText("State: --");
        }

        serverStateLabel.setText(proxy.getStateMessage());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        proxy.connect();
    }
}
