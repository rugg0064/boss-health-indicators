package com.boss.health.indicator.ui;

import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class PercentSpinner extends JSpinner {

    public PercentSpinner(double initialValue, double minValue, double maxValue, double stepSize) {
        super(new SpinnerNumberModel(initialValue, minValue, maxValue, stepSize));

        DecimalFormat percentFormat = new DecimalFormat("0.##%");
        NumberFormatter formatter = new NumberFormatter(percentFormat);
        formatter.setValueClass(Double.class);
        formatter.setMinimum(minValue);
        formatter.setMaximum(maxValue);
        formatter.setAllowsInvalid(true);

        JFormattedTextField textField = ((JSpinner.DefaultEditor) this.getEditor()).getTextField();

        JFormattedTextField.AbstractFormatter f = new JFormattedTextField.AbstractFormatter() {
            public String valueToString(Object value) throws ParseException {
                if (value == null) {
                    return "";
                }
                double percentValue = ((Number) value).doubleValue();
                return percentFormat.format(percentValue);
            }

            public Object stringToValue(String text) throws ParseException {
                if (!text.endsWith("%")) {
                    text += "%";
                }
                if (text == null || text.isEmpty()) {
                    return null;
                }
                double percentValue = percentFormat.parse(text).doubleValue();
                return percentValue;
            }
        };

        textField.setFormatterFactory(new DefaultFormatterFactory(f));
    }
}