package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.StockHawkApp;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity {

    private StockHawkApp app;

    @BindView(R.id.layoutRoot) ViewGroup root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (StockHawkApp)getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.setCurrentActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearCurrentActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCurrentActivity();
    }

    private void clearCurrentActivity(){
        if(this.equals(app.getCurrentActivity())) app.setCurrentActivity(null);
    }

    public void showSnackbar(String message, int duration){
        Snackbar.make(root, message, duration).show();
    }
}
