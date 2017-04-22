package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

class StockWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Intent intent;

    private Cursor data;

    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;

    public StockWidgetFactory(Context context, Intent intent){
        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {

        final long identityToken = Binder.clearCallingIdentity();

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        onDataSetChanged();

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDataSetChanged() {
        closeCursor();
        long identityToken = Binder.clearCallingIdentity();
        data = context.getContentResolver().query(
                Contract.Quote.URI,
                null,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL
        );
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        closeCursor();
    }

    @Override
    public int getCount() {
        return data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if(!rowExists(position)) return null;

        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_list_item_quote);
        return populateRemoteViews(view);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if(!rowExists(position)) return -1;
        return data.getLong(Contract.Quote.POSITION_ID);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private boolean rowExists(int position){
        return data != null && data.moveToPosition(position);
    }

    private void closeCursor(){
        if(data != null) data.close();
    }

    private RemoteViews populateRemoteViews(RemoteViews view){

        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);

        Intent symbolIntent = new Intent();
        symbolIntent.putExtra(context.getString(R.string.intent_stock_symbol), symbol);

        view.setTextViewText(R.id.symbol, symbol);
        view.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));
        view.setTextViewText(R.id.change,
                PrefUtils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_absolute_key))
                        ? change
                        : percentage);
        view.setInt(R.id.change, "setBackgroundResource",
                rawAbsoluteChange > 0
                        ? R.drawable.percent_change_pill_green
                        : R.drawable.percent_change_pill_red);

        view.setOnClickFillInIntent(R.id.layoutRoot, symbolIntent);

        return view;
    }
}
