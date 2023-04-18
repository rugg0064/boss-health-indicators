package com.boss.health.indicator;

import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;

class BossHealthIndicatorPanel extends PluginPanel  {
    private BossHealthIndicatorPlugin plugin;

    BossHealthIndicatorPanel(BossHealthIndicatorPlugin plugin)
    {
        this.plugin = plugin;

        setBorder(null);
        setLayout(new DynamicGridLayout(0, 1));

        JPanel panel = createPanel();

        add(panel);
    }


    public JPanel createPanel() {
        // Create main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add plugin name label
        JLabel nameLabel = new JLabel("Boss Health Indicator");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);

        // Add "Add Boss" button
        JButton addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(addButton);

        // Add list of bosses
        JPanel bossListPanel = new JPanel();
        bossListPanel.setLayout(new BoxLayout(bossListPanel, BoxLayout.Y_AXIS));
        JScrollPane bossListScrollPane = new JScrollPane(bossListPanel);
        bossListScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bossListScrollPane);

        // Create a panel for each boss
        for (int i = 0; i < 5; i++) {
            JPanel bossPanel = new JPanel();
            bossPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            bossPanel.setLayout(new BoxLayout(bossPanel, BoxLayout.Y_AXIS));

            // Add boss name label
            JLabel bossNameLabel = new JLabel("Kraken");
            bossNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            bossPanel.add(bossNameLabel);

            // Add boss health bar indicators
            JPanel indicatorsPanel = new JPanel();
            indicatorsPanel.setLayout(new BoxLayout(indicatorsPanel, BoxLayout.Y_AXIS));
            JScrollPane indicatorsScrollPane = new JScrollPane(indicatorsPanel);
            for (int j = 0; j < 2; j++) {
                JLabel label = new JLabel(50 + "% - " + Integer.toHexString(0x449955));
                JButton removeButton = new JButton("Remove");
                JPanel indicatorPanel = new JPanel();
                indicatorPanel.add(label);
                indicatorPanel.add(removeButton);
                indicatorsPanel.add(indicatorPanel);
            }
            indicatorsScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
            bossPanel.add(indicatorsScrollPane);

            // Add boss panel to boss list panel
            bossListPanel.add(bossPanel);
        }

        return panel;
    }

}
