package com.github.gfx.android.orma.example;

import com.facebook.stetho.Stetho;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);
    }
}
