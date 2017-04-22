package com.udacity.stockhawk;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;

public class StockHawkApp extends Application {

    private AppCompatActivity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }

    public void setCurrentActivity(AppCompatActivity activity){ currentActivity = activity; }
    public AppCompatActivity getCurrentActivity(){ return currentActivity; }
}
