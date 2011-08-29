/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.util;

import static java.lang.Math.min;

public class AvgValue {

    private final double[] values;
    private final int depth;
    private int valuesCount;
    private double currentSum;

    public AvgValue(int depth) {
        this.depth = depth;
        values = new double[depth];
    }

    public void addValue(double newValue) {
        currentSum = currentSum - values[valuesCount % values.length] + newValue;
        values[valuesCount % values.length] = newValue;
        valuesCount++;
    }

    public double getCurrentValue() {
        return currentSum / min(valuesCount, depth);
    }

    public String toString() {
        return String.format("Avg value = %10.5f", getCurrentValue());
    }
}