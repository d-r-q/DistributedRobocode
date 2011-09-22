/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Median {

    private final int limit;
    private final List<Double> values;

    public Median(int limit) {
        this.limit = limit;
        values = new ArrayList<Double>(limit);
    }

    public void addValue(double value) {
        if (values.size() == limit) {
            double m = getMedian();
            if (value < m) {
                values.remove(0);
            } else {
                values.remove(values.size() - 1);
            }
        }

        int idx = Collections.binarySearch(values, value);
        if (idx < 0) {
            idx = -idx - 1;
        }
        if (idx < values.size()) {
            values.add(idx, value);
        } else {
            values.add(value);
        }
    }

    public double getMedian() {
        if (values.size() == 0) {
            return 0;
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        int idx = values.size() / 2 - 1;
        return (values.get(idx) + values.get(idx + 1)) / 2;
    }

    public String toString() {
        return String.format("Median = %f", getMedian());
    }

    public List<Double> getValues() {
        return values;
    }

}
