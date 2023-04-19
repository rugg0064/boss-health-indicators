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

    static
    {
        final BufferedImage addIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/add_icon.png");
        final BufferedImage removeIconImage = ImageUtil.loadImageResource(BossHealthIndicatorPlugin.class, "/remove_icon.png");
        ADD_ICON = new ImageIcon(addIconImage);
        REMOVE_ICON = new ImageIcon(removeIconImage);
        ADD_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(addIconImage, 0.53f));
        REMOVE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(removeIconImage, 0.53f));
    }
}
