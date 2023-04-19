package com.boss.health.indicator.ui;

import com.boss.health.indicator.BossHealthIndicatorPlugin;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ColorPicker {
    private static final ImageIcon FILL_COLOR_ICON;
    private static final ImageIcon FILL_COLOR_HOVER_ICON;
    private static final ImageIcon NO_FILL_COLOR_ICON;
    private static final ImageIcon NO_FILL_COLOR_HOVER_ICON;
    private static final int DEFAULT_FILL_OPACITY = 75;

    static {
        final BufferedImage fillImg = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/color_icon.png");
        final BufferedImage fillImgHover = ImageUtil.luminanceOffset(fillImg, -150);
        FILL_COLOR_ICON = new ImageIcon(fillImg);
        FILL_COLOR_HOVER_ICON = new ImageIcon(fillImgHover);

        NO_FILL_COLOR_ICON = new ImageIcon(fillImgHover);
        NO_FILL_COLOR_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(fillImgHover, -100));
    }

    private Color color;
    private Runnable onChanged;
    private BossHealthIndicatorPlugin plugin;
    private JLabel label;

    public ColorPicker(Color color, BossHealthIndicatorPlugin plugin, Runnable onChanged) {
        this.color = color;
        this.onChanged = onChanged;
        this.plugin = plugin;

        label = new JLabel(FILL_COLOR_ICON);
        label.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                openColorPicker();
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                label.setIcon(color.getAlpha() == 0 ? NO_FILL_COLOR_HOVER_ICON : FILL_COLOR_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                label.setIcon(color.getAlpha() == 0 ? NO_FILL_COLOR_ICON : FILL_COLOR_ICON);
            }
        });
        updateColor();
    }

    private void openColorPicker()
    {
        RuneliteColorPicker colorPicker = plugin.getColorPickerManager().create(
                SwingUtilities.windowForComponent(label),
                color.getAlpha() == 0 ? ColorUtil.colorWithAlpha(color, DEFAULT_FILL_OPACITY) : color,
                "",
                false);
        //colorPicker.setLocation(getLocationOnScreen());
        colorPicker.setOnColorChange(c ->
        {
            color = c;
            updateColor();
            onChanged.run();
        });
        //colorPicker.setOnClose(c -> plugin.updateConfig());
        colorPicker.setVisible(true);
    }

    private void updateColor() {
        final boolean isFullyTransparent = color.getAlpha() == 0;
        if (isFullyTransparent)
        {
            label.setBorder(null);
        }
        else
        {
            Color fullColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
            label.setBorder(new MatteBorder(0, 0, 3, 0, fullColor));
        }
        label.setIcon(isFullyTransparent ? NO_FILL_COLOR_ICON : FILL_COLOR_ICON);
    }

    public Color getColor() {
        return color;
    }

    public JComponent getComponent() {
        return label;
    }
}
