package com.boss.health.indicator;

import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.ui.*;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
        //ColorPicker colorPicker = new ColorPicker(Color.BLUE, plugin, (Runnable) () -> {} );
        //panel.add(colorPicker.getComponent());

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JLabel titleLabel = new JLabel("Boss Health Indicators");
        IconButton addButton = new IconButton(Icons.ADD_ICON, Icons.ADD_ICON_HOVER, "Add a new boss", () -> {
            plugin.updateFromPanel(); });

        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(addButton);
        panel.add(topPanel);


        //panel.add(new ColorPicker(Color.BLUE, plugin, () -> {} ).getComponent());
        //panel.add(new PercentagePicker(0, () -> {}).getComponent());
        //panel.add(new IndicatorPicker(() -> {}, plugin).getComponent());

        for(BossIndicators bossIndicator : plugin.getBossDatabase()) {
            BossIndicatorCreator creator = new BossIndicatorCreator(bossIndicator, () -> { plugin.updateFromPanel(); }, plugin);
            bossIndicatorCreators.add(creator);
            panel.add(creator.getComponent());
        }

        return panel;
        /*
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        */
        /*
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
        */
        //panel.add(generatePercentagePanel("Zulrah"));
        //panel.add(generatePercentagePanel("Akkha"));
        //panel.add(generatePercentagePanel("Ba-Ba"));

        //return panel;
    }

    JLabel createButton(ImageIcon icon, ImageIcon hoverIcon, String tooltip) {
        JLabel addButton = new JLabel(icon);
        addButton.setToolTipText(tooltip);
        addButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                addButton.setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                addButton.setIcon(icon);
            }
        });
        return addButton;
    }

    /*
    public JPanel generatePercentagePanel(String panelName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the name label
        JLabel nameLabel = new JLabel(panelName);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create the add button
        JLabel addButton = createButton(ADD_ICON, ADD_ICON_HOVER, "Add a new marker");

        // Create the label and remove button panel
        JPanel labelAndRemovePanel = new JPanel(new BorderLayout());
        labelAndRemovePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel percentagesLabel = new JLabel("Percentages:");

        JLabel removeButton = createButton(REMOVE_ICON, REMOVE_ICON_HOVER, "Delete Marker");
        removeButton.setMaximumSize(new Dimension(100, 30));

        JPanel removeButtonPanel = new JPanel();
        removeButtonPanel.setLayout(new BoxLayout(removeButtonPanel, BoxLayout.X_AXIS));
        removeButtonPanel.add(Box.createHorizontalGlue());
        removeButtonPanel.add(removeButton);

        labelAndRemovePanel.add(percentagesLabel, BorderLayout.WEST);
        labelAndRemovePanel.add(removeButtonPanel, BorderLayout.EAST);

        // Create the percentages list panel
        JPanel percentagesPanel = new JPanel();
        percentagesPanel.setLayout(new BoxLayout(percentagesPanel, BoxLayout.Y_AXIS));

        List<JSpinner> percentageFields = new ArrayList<JSpinner>();
        percentageFields.add(createPercentageField());

        percentagesPanel.add(percentageFields.get(0));


        // Add components to the panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(nameLabel, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(labelAndRemovePanel, BorderLayout.CENTER);
        panel.add(percentagesPanel, BorderLayout.SOUTH);

        return panel;
    }
    */

    private JSpinner createPercentageField() {
        JSpinner percentageField = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        percentageField.setMaximumSize(new Dimension(48, percentageField.getPreferredSize().height));
        return percentageField;
    }

    public List<BossIndicators> getBossDatabase() {
        List<BossIndicators> returnList = new ArrayList<BossIndicators>();
        for(BossIndicatorCreator creator : bossIndicatorCreators) {
            returnList.add(creator.getBossIndicators());
        }
        return returnList;
    }
}
