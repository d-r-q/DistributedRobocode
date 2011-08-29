/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.ui;

import ru.jdev.rc.drc.client.Challenge;
import ru.jdev.rc.drc.client.RobocodeClient;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoPanel extends JPanel {

    private static final DateFormat finishTimeDateFormat = new SimpleDateFormat("HH:mm");

    private final Challenge challenge;
    private final RobocodeClient client;

    private JLabel execTimeLabel = new JLabel("Execution time:");
    private JLabel remainingTime = new JLabel("Estimated remaining time:");
    private JLabel estimatedFinishTime = new JLabel("Estimated finish time:");

    public InfoPanel(Challenge challenge, RobocodeClient client) {
        this.challenge = challenge;
        this.client = client;
    }

    public void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Challenge: " + challenge.getName()));

        final JPanel challengerPanel = new JPanel(new GridLayout(1, 3));
        challengerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, challengerPanel.getPreferredSize().height));
        challengerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, challengerPanel.getPreferredSize().height));

        final JLabel challengerLabel = new JLabel("Challenger: " + challenge.getChallenger().getNameAndVersion());
        challengerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        challengerPanel.add(challengerLabel);

        JLabel seasonsLabel = new JLabel("Seasons: " + challenge.getSeasons());
        seasonsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        challengerPanel.add(seasonsLabel);

        final JLabel roundsLabel = new JLabel("Rounds: " + challenge.getRounds());
        roundsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        challengerPanel.add(roundsLabel);

        add(challengerPanel);

        JPanel timingPanel = new JPanel(new GridLayout(1, 3));
        timingPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, timingPanel.getPreferredSize().height));

        execTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timingPanel.add(execTimeLabel);

        remainingTime.setHorizontalAlignment(SwingConstants.CENTER);
        timingPanel.add(remainingTime);

        estimatedFinishTime.setHorizontalAlignment(SwingConstants.CENTER);
        timingPanel.add(estimatedFinishTime);

        add(timingPanel);
    }

    public void update() {
        if (client.getFinishTime() == -1) {
            execTimeLabel.setText("Elapsed time: " + RobocodeClient.executionTimeDateFormat.format(new Date(System.currentTimeMillis() - client.getStartTime())));
        } else {
            execTimeLabel.setText("Execution time: " + RobocodeClient.executionTimeDateFormat.format(new Date(client.getFinishTime() - client.getStartTime())));
        }
        remainingTime.setText("Estimated remaining time: " + RobocodeClient.executionTimeDateFormat.format(new Date(client.getEstimatedRemainingTime())));
        estimatedFinishTime.setText("Estimated finish time: " + finishTimeDateFormat.format(new Date(System.currentTimeMillis() + client.getEstimatedRemainingTime())));

        validate();
        repaint();
    }
}
