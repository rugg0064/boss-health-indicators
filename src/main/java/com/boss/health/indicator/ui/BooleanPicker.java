package com.boss.health.indicator.ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Creates a picker that returns a boolean, has two sets of icons for true/false
public class BooleanPicker extends JLabel {
    private final ImageIcon iconTrue;
    private final ImageIcon iconTrueHover;
    private final ImageIcon iconFalse;
    private final ImageIcon iconFalseHover;

    private boolean selected;

    public BooleanPicker(ImageIcon iconTrue, ImageIcon iconTrueHover, ImageIcon iconFalse, ImageIcon iconFalseHover, boolean initialValue, String tooltip, Runnable onChanged) {
        super(initialValue ? iconTrue : iconFalse);
        this.selected = initialValue;
        this.iconTrue = iconTrue;
        this.iconTrueHover = iconTrueHover;
        this.iconFalse = iconFalse;
        this.iconFalseHover = iconFalseHover;
        setToolTipText(tooltip);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                selected = !selected;
                setIcon(selected ? iconTrueHover : iconFalseHover);
                onChanged.run();
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                setIcon(selected ? iconTrueHover : iconFalseHover);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                setIcon(selected ?  iconTrue: iconFalse);
            }
        });
    }

    public boolean getValue() {
        return selected;
    }
}
