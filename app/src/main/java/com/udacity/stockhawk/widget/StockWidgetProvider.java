package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailActivity;

public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds){
            updateWidget(context, appWidgetManager, id);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);

        // Intent for header click
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent for list item click
        Intent stockDetailActivityIntent = new Intent(context, StockDetailActivity.class);
        PendingIntent stockDetailActivityPendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(stockDetailActivityIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent for RemoteViewsService for Stock ListView
        Intent serviceIntent = new Intent(context, StockWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setOnClickPendingIntent(R.id.stock_widget_header, mainActivityPendingIntent);
        views.setRemoteAdapter(R.id.stock_widget_stocks, serviceIntent);
        views.setPendingIntentTemplate(R.id.stock_widget_stocks, stockDetailActivityPendingIntent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // handle broadcast from updated stocks here
        String action = intent.getAction();
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(action)){
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, getClass()));
            manager.notifyAppWidgetViewDataChanged(ids, R.id.stock_widget_stocks);
        }
    }
}
