package com.boss.health.indicator.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.NumberFormat;

public class PercentagePicker {
    private JSpinner spinner;

    public PercentagePicker(double percent, Runnable onChanged) {
        this.spinner = new PercentSpinner(percent, 0, 1, 0.01);
        this.spinner.addChangeListener((e) -> {
            onChanged.run();
        });
        this.spinner.setMaximumSize(new Dimension(72, spinner.getPreferredSize().height));

    }

    public JComponent getComponent() {
        return this.spinner;
    }

    public double getPercent() {
        return (double) spinner.getValue();
    }
}
