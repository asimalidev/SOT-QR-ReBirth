package com.qrcodescanner.barcodereader.qrgenerator.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    public BaseActivity context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = BaseActivity.this;
//        MyLocaleHelper.updateConfig(MyApplication.getApplication(), getBaseContext().getResources().getConfiguration());
    }


    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(MyLocaleHelper.onAttach(base, "en"));
        super.attachBaseContext(base);
        Configuration config = getApplicationContext().getResources().getConfiguration();
        getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
        Log.e("TAG", "attachBaseContext: " + config);
    }
}
