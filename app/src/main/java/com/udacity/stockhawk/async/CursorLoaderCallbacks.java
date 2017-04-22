package com.udacity.stockhawk.async;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockDetailActivity;

public class CursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private CursorLoaderFinishedHandler handler;

    public CursorLoaderCallbacks(Context context, CursorLoaderFinishedHandler handler){
        this.context = context;
        this.handler = handler;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id){
            case StockDetailActivity.STOCK_HISTORY_LOADER_ID:
                String symbol;
                if(args != null && (symbol = args.getString(StockDetailActivity.SYMBOL_LOADER_ARGS)) != null) {
                    return new CursorLoader(context,
                            Contract.Quote.makeUriForStock(symbol),
                            new String[]{Contract.Quote.COLUMN_HISTORY},
                            null,
                            null,
                            null);
                }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        handler.cursorLoaderFinished(loader, data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public interface CursorLoaderFinishedHandler{
        void cursorLoaderFinished(Loader<Cursor> loader, Cursor data);
    }
}
