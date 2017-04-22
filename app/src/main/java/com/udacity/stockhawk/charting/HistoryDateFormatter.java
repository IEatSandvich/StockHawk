package com.udacity.stockhawk.charting;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.ui.StockDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryDateFormatter implements IAxisValueFormatter {

    private List<Long> xValues;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

    public HistoryDateFormatter(List<Long> xValues) {
        this.xValues = xValues;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long xValue = xValues.get((int)value);
        return dateFormatter.format(new Date(xValue));
    }
}
