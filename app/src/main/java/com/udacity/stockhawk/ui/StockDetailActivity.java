package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.async.CursorLoaderCallbacks;
import com.udacity.stockhawk.charting.HistoryDateFormatter;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends BaseActivity implements CursorLoaderCallbacks.CursorLoaderFinishedHandler {

    public static final int STOCK_HISTORY_LOADER_ID = 5000;
    public static final String SYMBOL_LOADER_ARGS = "symbol";

    public CursorLoaderCallbacks historyLoaderCallbacks;

    @BindView(R.id.stockHistoryChart) LineChart lineChart;
    @BindView(R.id.errorTextView) TextView errorTextView;

    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        Intent sentIntent = getIntent();

        if(TextUtils.isEmpty(symbol = sentIntent.hasExtra(getString(R.string.intent_stock_symbol)) ? sentIntent.getStringExtra(getString(R.string.intent_stock_symbol)) : "")) { backPressed(); return; }

        setTitle(symbol);

        historyLoaderCallbacks = new CursorLoaderCallbacks(this, this);
        Bundle args = new Bundle();
        args.putString(SYMBOL_LOADER_ARGS, symbol);
        getLoaderManager().initLoader(STOCK_HISTORY_LOADER_ID, args, historyLoaderCallbacks);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stock_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                backPressed();
                return true;
            case R.id.action_zoom_out:
                zoomOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getHistoryFromCursor(Cursor cursor){
        if(cursor == null || cursor.getCount() == 0) { showError(getString(R.string.symbol_not_found, symbol)); return null; }

        cursor.moveToFirst();
        String history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

        if(TextUtils.isEmpty(history)) { showError(getString(R.string.no_history_found_for_symbol, symbol)); return null; }

        return history;
    }

    private void showError(String message){
        errorTextView.setText(message);
        lineChart.setVisibility(View.GONE);
    }

    private void handleHistoryData(String historyCSV){
        CSVReader reader = new CSVReader(new StringReader(historyCSV));

        List<String[]> lines = getLines(reader);
        if(lines.size() <= 0) { showError(getString(R.string.no_history_found_for_symbol, symbol)); return; }

        int size = lines.size();
        List<Entry> entries = new ArrayList<>(size);
        List<Long> xValues = new ArrayList<>(size);
        int counter = 0;
        for(String line[] : lines){
            xValues.add(Long.valueOf(line[0]));
            entries.add(new Entry(
                    counter++,
                    Float.valueOf(line[1])
                )
            );
        }

        configureAndPopulateChart(xValues, entries);
    }

    private List<String[]> getLines(CSVReader reader){
        List<String[]> lines = new ArrayList<>();

        try {
            // reverse list to put in chronological order
            lines = Lists.reverse(reader.readAll());
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return lines;
    }

    private void configureAndPopulateChart(List<Long> xValues, List<Entry> entries) {
        int lineColor = Color.argb(255, 224, 224, 224);
        int textColor = Color.argb(255, 224, 224, 224);
        int highlightColor = Color.argb(255, 211, 47, 47);
        int backgroundColor = Color.argb(255, 21, 21, 21);
        int axisTextSize = 16;
        float lineWidth = 2f;
        float circleRadius = 6f;
        float circleHoleRadius = 4f;
        int xAxisRotation = -45;
        String desc = "Stock price history of " + symbol;

        XAxis axis = lineChart.getXAxis();
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setDrawGridLines(false);
        axis.setLabelRotationAngle(xAxisRotation);
        axis.setTextColor(textColor);
        axis.setValueFormatter(new HistoryDateFormatter(xValues));
        axis.setTextSize(axisTextSize);
        axis.setLabelCount(8);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(textColor);
        leftAxis.setTextSize(axisTextSize);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(textColor);
        rightAxis.setTextSize(axisTextSize);

        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setDrawValues(false);
        dataSet.setColor(lineColor);
        dataSet.setCircleColor(lineColor);
        dataSet.setCircleColorHole(backgroundColor);
        dataSet.setHighLightColor(highlightColor);
        dataSet.setLineWidth(lineWidth);
        dataSet.setCircleRadius(circleRadius);
        dataSet.setCircleHoleRadius(circleHoleRadius);

        LineData lineData = new LineData(dataSet);

        lineChart.setDescription(null);
        lineChart.getLegend().setEnabled(false);
        lineChart.setBackgroundColor(backgroundColor);
        lineChart.setExtraOffsets(4, 4, 4, 4);

        lineChart.setData(lineData);
        lineChart.setContentDescription(desc);
    }

    private void backPressed(){
        NavUtils.navigateUpFromSameTask(this);
    }

    private void zoomOut(){
        if(lineChart.isFullyZoomedOut()) return;
        lineChart.zoomOut();
    }

    @Override
    public void cursorLoaderFinished(Loader<Cursor> loader, Cursor data) {
        String history;
        if(TextUtils.isEmpty(history = getHistoryFromCursor(data))) return;

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        handleHistoryData(history);
    }
}
