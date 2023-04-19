package com.boss.health.indicator.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextPicker {
    private JTextField textField;

    public TextPicker(String startingText, Runnable onChanged) {
        textField = new JTextField();
        textField.setText(startingText);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChanged.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChanged.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChanged.run();
            }
        });
    }

    public String getText() {
        return textField.getText();
    }

    public JComponent getComponent() {
        return textField;
    }
}
