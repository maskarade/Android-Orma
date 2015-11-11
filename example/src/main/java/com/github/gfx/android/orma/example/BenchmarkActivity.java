package com.github.gfx.android.orma.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BenchmarkActivity extends AppCompatActivity {

    public static Intent createIntent(Context context) {
        return new Intent(context, BenchmarkActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benchmark);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}
