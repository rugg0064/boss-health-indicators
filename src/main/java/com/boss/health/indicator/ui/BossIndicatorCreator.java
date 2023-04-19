package com.boss.health.indicator.ui;

import com.boss.health.indicator.BossHealthIndicatorPlugin;
import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.model.Indicator;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BossIndicatorCreator {
    private TextPicker bossNamePicker;
    private List<IndicatorPicker> indicatorPickers;
    private JPanel panel;

    public BossIndicatorCreator(BossIndicators bossIndicators, Runnable onChanged, BossHealthIndicatorPlugin plugin) {
        this.bossNamePicker = new TextPicker(bossIndicators.getBossName(), onChanged);
        this.indicatorPickers = new ArrayList<IndicatorPicker>();
        for(Indicator indicator : bossIndicators.getEntries()) {
            indicatorPickers.add(new IndicatorPicker(indicator, onChanged, plugin));
        }
        this.panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(bossNamePicker.getComponent());
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new IconButton(Icons.ADD_ICON, Icons.ADD_ICON_HOVER, "Add a new marker", () -> {}));
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new IconButton(Icons.REMOVE_ICON, Icons.REMOVE_ICON_HOVER, "Remove boss", () -> {}));
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel indicatorsPanel = new JPanel();
        indicatorsPanel.setLayout(new BoxLayout(indicatorsPanel, BoxLayout.Y_AXIS));
        for(IndicatorPicker picker : indicatorPickers) {
            JComponent component = picker.getComponent();
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            indicatorsPanel.add(picker.getComponent());
        }
        panel.add(indicatorsPanel);
    }

    public JComponent getComponent() {
        return panel;
    }

    public BossIndicators getBossIndicators() {
        ArrayList<Indicator> indicators = new ArrayList<Indicator>();
        for(IndicatorPicker indicatorPicker : indicatorPickers) {
            Indicator indicator = indicatorPicker.getIndicator();
            indicators.add(indicator);
        }
        return new BossIndicators(bossNamePicker.getText(), indicators);
    }
}
