package com.boss.health.indicator.model;

import java.awt.*;

public class Indicator {
    private double percentage;
    private Color color;
    boolean notify;

    public Indicator(double percentage, Color color, boolean notify) {
        this.percentage = percentage;
        this.color = color;
        this.notify = notify;
    }

    public double getPercentage() {
        return percentage;
    }

    public Color getColor() {
        return color;
    }

    public boolean getNotify() {
        return notify;
    }

    public boolean hasAnyNull() {
        return color == null;
    }
}