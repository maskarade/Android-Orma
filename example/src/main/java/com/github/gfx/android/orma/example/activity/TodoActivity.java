package com.github.gfx.android.orma.example.activity;

import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityTodoBinding;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TodoActivity extends AppCompatActivity {

    public static Intent createIntent(Context context) {
        return new Intent(context, TodoActivity.class);
    }

    OrmaDatabase orma;

    ActivityTodoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_todo);

        orma = new OrmaDatabase(this, "main.db");
    }
}
