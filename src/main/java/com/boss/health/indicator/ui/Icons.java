package com.boss.health.indicator.ui;

import com.boss.health.indicator.BossHealthIndicatorPlugin;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class Icons {
    public static final ImageIcon ADD_ICON;
    public static final ImageIcon ADD_ICON_HOVER;
    public static final ImageIcon REMOVE_ICON;
    public static final ImageIcon REMOVE_ICON_HOVER;
    public static final ImageIcon IMPORT_ICON;
    public static final ImageIcon IMPORT_ICON_HOVER;
    public static final ImageIcon EXPORT_ICON;
    public static final ImageIcon EXPORT_ICON_HOVER;
    public static final ImageIcon RIGHT_ARROW_ICON;
    public static final ImageIcon RIGHT_ARROW_ICON_HOVER;
    public static final ImageIcon LEFT_ARROW_ICON;
    public static final ImageIcon LEFT_ARROW_ICON_HOVER;
    public static final ImageIcon DOWN_ARROW_ICON;
    public static final ImageIcon DOWN_ARROW_ICON_HOVER;
    public static final ImageIcon UP_ARROW_ICON;
    public static final ImageIcon UP_ARROW_ICON_HOVER;
    public static final ImageIcon BELL_ENABLED;
    public static final ImageIcon BELL_ENABLED_HOVER;
    public static final ImageIcon BELL_DISABLED;
    public static final ImageIcon BELL_DISABLED_HOVER;


    static
    {
        final BufferedImage addIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/add_icon.png");
        ADD_ICON = new ImageIcon(addIconImage);
        ADD_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(addIconImage, 0.53f));;

        final BufferedImage removeIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/remove_icon.png");
        REMOVE_ICON = new ImageIcon(removeIconImage);
        REMOVE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(removeIconImage, 0.53f));

        final BufferedImage importIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/import_icon.png");
        IMPORT_ICON = new ImageIcon(importIconImage);
        IMPORT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(importIconImage, 0.53f));

        final BufferedImage exportIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/export_icon.png");
        EXPORT_ICON = new ImageIcon(exportIconImage);
        EXPORT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(exportIconImage, 0.53f));

        final BufferedImage rightArrowIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/arrow_right.png");
        RIGHT_ARROW_ICON = new ImageIcon(rightArrowIconImage);
        RIGHT_ARROW_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(rightArrowIconImage, 0.53f));

        final BufferedImage leftArrowIconImage = ImageUtil.flipImage(rightArrowIconImage, true, true);
        LEFT_ARROW_ICON = new ImageIcon(leftArrowIconImage);
        LEFT_ARROW_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(leftArrowIconImage, 0.53f));

        final BufferedImage downArrowIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/arrow_down.png");
        DOWN_ARROW_ICON = new ImageIcon(downArrowIconImage);
        DOWN_ARROW_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(downArrowIconImage, 0.53f));

        final BufferedImage upArrowIconImage = ImageUtil.flipImage(downArrowIconImage, true, true);
        UP_ARROW_ICON = new ImageIcon(upArrowIconImage);
        UP_ARROW_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(upArrowIconImage, 0.53f));

        final BufferedImage bellEnabledIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/bell_icon_enabled.png");
        BELL_ENABLED = new ImageIcon(bellEnabledIconImage);
        BELL_ENABLED_HOVER = new ImageIcon(ImageUtil.alphaOffset(bellEnabledIconImage, 0.53f));

        final BufferedImage bellDisabledIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/bell_icon_disabled.png");
        BELL_DISABLED = new ImageIcon(bellDisabledIconImage);
        BELL_DISABLED_HOVER = new ImageIcon(ImageUtil.alphaOffset(bellDisabledIconImage, 0.53f));

    }
}
