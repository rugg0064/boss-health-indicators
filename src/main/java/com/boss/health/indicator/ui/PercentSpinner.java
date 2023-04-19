package com.boss.health.indicator.ui;

import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class PercentSpinner extends JSpinner {

    public PercentSpinner(double initialValue, double minValue, double maxValue, double stepSize) {
        super(new SpinnerNumberModel(initialValue, minValue, maxValue, stepSize));

        // Create a formatter that appends a "%" symbol to the formatted text
        DecimalFormat percentFormat = new DecimalFormat("0.##%");
        NumberFormatter formatter = new NumberFormatter(percentFormat);
        formatter.setValueClass(Double.class);
        formatter.setMinimum(minValue);
        formatter.setMaximum(maxValue);
        formatter.setAllowsInvalid(true);

        // Set the formatter on the editor of the JFormattedTextField
        JFormattedTextField textField = ((JSpinner.DefaultEditor) this.getEditor()).getTextField();
        textField.setFormatterFactory(new DefaultFormatterFactory(formatter));

    }
}
