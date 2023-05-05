package com.boss.health.indicator;

import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.ui.*;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class BossHealthIndicatorPanel extends PluginPanel  {

    private BossHealthIndicatorPlugin plugin;
    private List<BossIndicatorCreator> bossIndicatorCreators;

    IconButton expandIcon;
    IconButton collapseIcon;
    JPanel panel;

    BossHealthIndicatorPanel(BossHealthIndicatorPlugin plugin)
    {
        this.plugin = plugin;

        bossIndicatorCreators = new ArrayList<BossIndicatorCreator>();

        setBorder(null);
        setLayout(new DynamicGridLayout(0, 1));

        panel = createPanel();
        fixCollapseButton();
        add(panel);
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Runnable onChanged = () -> {
            fixCollapseButton();
            plugin.updateFromPanel();
        };
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
        expandIcon = new IconButton(Icons.RIGHT_ARROW_ICON, Icons.RIGHT_ARROW_ICON_HOVER, "Expand all", () -> {
            setAllCollapsed(false);
        });
        collapseIcon = new IconButton(Icons.DOWN_ARROW_ICON, Icons.DOWN_ARROW_ICON_HOVER, "Collapse all", () -> {
            setAllCollapsed(true);
        });

        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(importButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(exportButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(addButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(collapseIcon);
        topPanel.add(expandIcon);
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
        fixCollapseButton();
        revalidate();
    }

    private void fixCollapseButton() {
        boolean anyAreExpanded = false;
        for(BossIndicatorCreator creator : bossIndicatorCreators) {
            if(!creator.isCollapsed()) {
                anyAreExpanded = true;
            }
        }
        expandIcon.setVisible(!anyAreExpanded);
        collapseIcon.setVisible(anyAreExpanded);
        panel.revalidate();
    }

    private void setAllCollapsed(boolean isCollapsed) {
        for(BossIndicatorCreator creator : bossIndicatorCreators) {
            creator.setCollapsed(isCollapsed);
        }
    }

    public void moveCreator(BossIndicatorCreator creator, int amount) {
        int creatorIndex = -1;
        for(int i = 0; i < bossIndicatorCreators.size() && creatorIndex == -1; i++) {
            if(bossIndicatorCreators.get(i) == creator) {
                creatorIndex = i;
            }
        }
        if(creatorIndex == -1) {
            System.out.println("Not found");
            return;
        }
        int goalIndex = creatorIndex + amount;
        goalIndex = Math.max(0, goalIndex);
        goalIndex = Math.min(bossIndicatorCreators.size() - 1, goalIndex);
        System.out.printf("Moving from index %d to index %d%n", creatorIndex, goalIndex);
        bossIndicatorCreators.remove(creatorIndex);
        bossIndicatorCreators.add(goalIndex, creator);
        plugin.updateFromPanel();
        remove(creator.getComponent());
        panel.add(creator.getComponent(), goalIndex + 1); // +1 because the top bar is also a child
        revalidate();
    }
}
