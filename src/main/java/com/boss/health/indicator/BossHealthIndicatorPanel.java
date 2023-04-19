package com.boss.health.indicator;

import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.ui.*;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class BossHealthIndicatorPanel extends PluginPanel  {

    private BossHealthIndicatorPlugin plugin;
    private List<BossIndicatorCreator> bossIndicatorCreators;

    BossHealthIndicatorPanel(BossHealthIndicatorPlugin plugin)
    {
        this.plugin = plugin;

        bossIndicatorCreators = new ArrayList<BossIndicatorCreator>();

        setBorder(null);
        setLayout(new DynamicGridLayout(0, 1));

        JPanel panel = createPanel();

        add(panel);
    }


    public JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Runnable onChanged = () -> { plugin.updateFromPanel(); };
        SelfRunnable<BossIndicatorCreator> onDelete = (self) -> {
            bossIndicatorCreators.remove(self);
            panel.remove(self.getComponent());
            panel.revalidate();
            onChanged.run();
        };

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JLabel titleLabel = new JLabel("Boss Health Indicators");
        IconButton importButton = new IconButton(Icons.IMPORT_ICON, Icons.IMPORT_ICON_HOVER, "Import markers from clipboard", () -> {
            plugin.importFromClipboard();
        });
        IconButton exportButton = new IconButton(Icons.EXPORT_ICON, Icons.EXPORT_ICON_HOVER, "Export markers to clipboard", () -> {
            plugin.exportToClipboard();
        });
        IconButton addButton = new IconButton(Icons.ADD_ICON, Icons.ADD_ICON_HOVER, "Add a new boss", () -> {
            createEmptyBossIndicatorCreator(panel, onChanged, onDelete);
            plugin.updateFromPanel();
        });

        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(importButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(exportButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(addButton);
        panel.add(topPanel);

        List<BossIndicators> database = plugin.getBossDatabase();
        for(BossIndicators bossIndicator : database) {
            BossIndicatorCreator creator = new BossIndicatorCreator(
                bossIndicator,
                onChanged,
                plugin,
                onDelete
            );
            bossIndicatorCreators.add(creator);
            panel.add(creator.getComponent());
        }

        return panel;
    }

    private void createEmptyBossIndicatorCreator(JPanel panel, Runnable onChanged, SelfRunnable<BossIndicatorCreator> onDelete) {
        BossIndicatorCreator creator = new BossIndicatorCreator(
                onChanged,
                plugin,
                onDelete
        );
        bossIndicatorCreators.add(creator);
        panel.add(creator.getComponent());
        panel.revalidate();
    }

    public List<BossIndicators> getBossDatabase() {
        List<BossIndicators> returnList = new ArrayList<BossIndicators>();
        for(BossIndicatorCreator creator : bossIndicatorCreators) {
            returnList.add(creator.getBossIndicators());
        }
        return returnList;
    }

    public void rebuild() {
        removeAll();
        bossIndicatorCreators.clear();
        JPanel panel = createPanel();
        add(panel);
        revalidate();
    }
}
