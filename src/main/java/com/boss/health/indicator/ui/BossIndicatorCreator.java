package com.boss.health.indicator.ui;

import com.boss.health.indicator.BossHealthIndicatorPanel;
import com.boss.health.indicator.BossHealthIndicatorPlugin;
import com.boss.health.indicator.SelfRunnable;
import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.model.Indicator;
import net.runelite.client.events.ConfigChanged;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BossIndicatorCreator {
    private TextPicker bossNamePicker;
    private List<IndicatorPicker> indicatorPickers;
    private JPanel indicatorsPanel;
    private Runnable onChanged;
    private BossHealthIndicatorPlugin plugin;
    private SelfRunnable<BossIndicatorCreator> onDelete;
    private JPanel panel;

    private boolean collapsed = false;
    private IconButton collapseButton;
    private IconButton expandButton;

    public BossIndicatorCreator(BossIndicators bossIndicators, Runnable onChanged, BossHealthIndicatorPlugin plugin, SelfRunnable<BossIndicatorCreator> onDelete) {
        this.onChanged = onChanged;
        this.plugin = plugin;
        this.onDelete = onDelete;
        this.bossNamePicker = new TextPicker(bossIndicators.getBossName(), onChanged);
        this.indicatorPickers = new ArrayList<IndicatorPicker>();
        this.indicatorsPanel = new JPanel();
        for(Indicator indicator : bossIndicators.getEntries()) {
            indicatorPickers.add(new IndicatorPicker(
                indicator,
                onChanged,
                plugin,
                (self) -> {
                    indicatorPickers.remove(self);
                    indicatorsPanel.remove(self.getComponent());
                    indicatorsPanel.revalidate();
                    onChanged.run();
                }));
        }
        createPanel();
    }

    public BossIndicatorCreator(Runnable onChanged, BossHealthIndicatorPlugin plugin, SelfRunnable<BossIndicatorCreator> onDelete) {
        this.onChanged = onChanged;
        this.plugin = plugin;
        this.onDelete = onDelete;
        this.bossNamePicker = new TextPicker("", onChanged);
        this.indicatorPickers = new ArrayList<IndicatorPicker>();
        this.indicatorsPanel = new JPanel();
        createPanel();
    }

    private void createPanel() {
        this.panel = new JPanel(new BorderLayout());
        panel.removeAll();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 2));

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        expandButton = new IconButton(Icons.RIGHT_ARROW_ICON, Icons.RIGHT_ARROW_ICON_HOVER, "Expand", () -> { setCollapsed(false); });
        collapseButton = new IconButton(Icons.DOWN_ARROW_ICON, Icons.DOWN_ARROW_ICON_HOVER, "Collapse", () -> { setCollapsed(true); });
        expandButton.setVisible(collapsed);
        collapseButton.setVisible(!collapsed);
        topPanel.add(expandButton);
        topPanel.add(collapseButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(bossNamePicker.getComponent());
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(new IconButton(Icons.ADD_ICON, Icons.ADD_ICON_HOVER, "Add a new marker", () -> {
            setCollapsed(false);
            IndicatorPicker newIndicatorPicker = new IndicatorPicker(
                onChanged,
                plugin,
                (self) -> {
                    indicatorPickers.remove(self);
                    JComponent component = self.getComponent();
                    component.setAlignmentX(Component.LEFT_ALIGNMENT);
                    indicatorsPanel.remove(component);
                    indicatorsPanel.revalidate();
                    onChanged.run();
                });
            indicatorPickers.add(newIndicatorPicker);
            newIndicatorPicker.getComponent().setAlignmentX(Component.LEFT_ALIGNMENT);
            indicatorsPanel.add(newIndicatorPicker.getComponent());
            indicatorsPanel.revalidate();
            onChanged.run();
        }));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(new IconButton(Icons.REMOVE_ICON, Icons.REMOVE_ICON_HOVER, "Remove boss", () -> { onDelete.run(this); }));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(new IconButton(Icons.UP_ARROW_ICON, Icons.UP_ARROW_ICON_HOVER, "Move up", () -> {
            plugin.moveCreator(this, -1);
        }));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(new IconButton(Icons.DOWN_ARROW_ICON, Icons.DOWN_ARROW_ICON_HOVER, "Move down", () -> {
            plugin.moveCreator(this, 1);
        }));
        panel.add(topPanel, BorderLayout.NORTH);

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

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean isCollapsed) {
        collapsed = isCollapsed;
        expandButton.setVisible(isCollapsed);
        collapseButton.setVisible(!isCollapsed);
        indicatorsPanel.setVisible(!isCollapsed);
        panel.revalidate();
        onChanged.run();
    }
}
