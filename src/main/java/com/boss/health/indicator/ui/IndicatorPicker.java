package com.boss.health.indicator.ui;

import com.boss.health.indicator.BossHealthIndicatorPlugin;
import com.boss.health.indicator.SelfRunnable;
import com.boss.health.indicator.model.Indicator;

import javax.swing.*;
import java.awt.*;

public class IndicatorPicker {
    private JPanel panel;
    private Runnable onChanged;

    private PercentagePicker percentagePicker;
    private ColorPicker colorPicker;

    public IndicatorPicker(Indicator indicator, Runnable onChanged, BossHealthIndicatorPlugin plugin, SelfRunnable<IndicatorPicker> onDelete) {
        this.onChanged = onChanged;

        panel = new JPanel();
        percentagePicker = new PercentagePicker(indicator.getPercentage(), onChanged);
        colorPicker = new ColorPicker(indicator.getColor(), plugin, onChanged);

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(colorPicker.getComponent());
        panel.add(Box.createHorizontalStrut(10));
        panel.add(percentagePicker.getComponent());
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new IconButton(Icons.REMOVE_ICON, Icons.REMOVE_ICON_HOVER, "Remove Indicator", () -> { onDelete.run(this); }));
    }

    public IndicatorPicker(Runnable onChanged, BossHealthIndicatorPlugin plugin, SelfRunnable<IndicatorPicker> onDelete) {
        this(new Indicator(0, Color.WHITE), onChanged, plugin, onDelete);
    }

    public JComponent getComponent() {
        return this.panel;
    }

    public Indicator getIndicator() {
        return new Indicator(percentagePicker.getPercent(), colorPicker.getColor());
    }
}
