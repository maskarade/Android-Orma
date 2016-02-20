/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma.example.activity;

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.example.BuildConfig;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityMainBinding;
import com.github.gfx.android.orma.example.orma.Category;
import com.github.gfx.android.orma.example.orma.Item;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.TraceListener;

import org.threeten.bp.ZonedDateTime;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String TAG = MainActivity.class.getSimpleName();

    static final String ORMA_SITE = "https://github.com/gfx/Android-Orma/";

    ActivityMainBinding activityMain;

    OrmaDatabase orma;

    public void largeLog(String tag, final String content) {
        if (content.length() > 2000) {
            Log.e(tag, content.substring(0, 2000));
            largeLog(tag, content.substring(2000));
        } else {
            Log.e(tag, content);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMain = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = activityMain.appBarMain.toolbar;
        setSupportActionBar(toolbar);

        FloatingActionButton fab = activityMain.appBarMain.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, ORMA_SITE, Snackbar.LENGTH_LONG)
                        .setAction("Open", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ORMA_SITE));
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });

        DrawerLayout drawer = activityMain.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = activityMain.navView;
        navigationView.setNavigationItemSelectedListener(this);

        activityMain.appBarMain.contentMain.textInfo.setText(
                "This is an example app for Orma v" + BuildConfig.VERSION_NAME + ".");

        final ArrayAdapter<String> logsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        activityMain.appBarMain.contentMain.listLogs.setAdapter(logsAdapter);

        orma = OrmaDatabase.builder(this)
                .migrationTraceListener(new TraceListener() {
                    @Override
                    public void onTrace(@NonNull MigrationEngine engine, @NonNull final String format,
                            @NonNull final Object[] args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logsAdapter.addAll(String.format(Locale.getDefault(), format, args));
                            }
                        });

                        TraceListener.LOGCAT.onTrace(engine, format, args);
                    }
                })
                .build();

        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    orma.migrate(); // may throws SQLiteConstraintException

                    simpleCRUD();
                    associations();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

                    largeLog(TAG, Log.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * Demonstrates simple CRUD operations, which makes no sense though.
     */
    void simpleCRUD() {
        // create
        Todo todo = new Todo();
        todo.title = "buy";
        todo.content = "milk banana apple";
        todo.createdTime = new Date();
        orma.insertIntoTodo(todo);

        // read
        todo = orma.selectFromTodo()
                .titleEq("buy")
                .value();

        // update
        orma.updateTodo()
                .titleEq("buy")
                .done(true)
                .execute();

        // delete
        orma.deleteFromTodo()
                .doneEq(true)
                .execute();
    }

    void associations() {
        orma.deleteFromCategory().execute();
        orma.deleteFromItem().execute();

        Category category = orma.relationOfCategory().getOrCreate(0, new ModelFactory<Category>() {
            @NonNull
            @Override
            public Category call() {
                return new Category("foo");
            }
        });

        Item item = category.createItem(orma, ZonedDateTime.now().toString());
        Log.d(TAG, "created: " + item);

        Log.d(TAG, "A category has many items (" + category.getItems(orma).count() + ")");
        Log.d(TAG, TextUtils.join(", ", category.getItems(orma)));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = activityMain.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_recycler_view) {
            startActivity(RecyclerViewActivity.createIntent(this));
        } else if (id == R.id.nav_list_view) {
            startActivity(ListViewActivity.createIntent(this));
        } else if (id == R.id.nav_benchmark) {
            startActivity(BenchmarkActivity.createIntent(this));
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_send) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show();
        }

        activityMain.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
