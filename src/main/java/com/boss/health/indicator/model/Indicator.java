package com.boss.health.indicator.model;

import java.awt.*;

public class Indicator {
    private double percentage;
    private Color color;

    public Indicator(double percentage, Color color) {
        this.percentage = percentage;
        this.color = color;
    }

    public double getPercentage() {
        return percentage;
    }

    public Color getColor() {
        return color;
    }

    public boolean hasAnyNull() {
        return color == null;
    }
}