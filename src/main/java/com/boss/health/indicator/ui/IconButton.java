package com.boss.health.indicator.ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconButton extends JLabel {
    public IconButton(ImageIcon icon, ImageIcon hoverIcon, String tooltip, Runnable onPressed) {
        super(icon);
        setToolTipText(tooltip);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                onPressed.run();
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                setIcon(icon);
            }
        });
    }

}
