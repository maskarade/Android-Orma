package com.github.gfx.android.orma.example;

import com.raizlabs.android.dbflow.config.FlowManager;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FlowManager.init(this);
    }
}
