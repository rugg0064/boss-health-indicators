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
    }
}
