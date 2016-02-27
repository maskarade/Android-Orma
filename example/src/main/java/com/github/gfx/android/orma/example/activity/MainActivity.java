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
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.TraceListener;

import org.threeten.bp.ZonedDateTime;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

    static final String DB_NAME = "main.db";

    ArrayAdapter<String> logsAdapter;

    ActivityMainBinding binding;

    OrmaDatabase orma;

    public static void largeLogE(String tag, String content) {
        if (content.length() > 2000) {
            Log.e(tag, content.substring(0, 2000));
            largeLogE(tag, content.substring(2000));
        } else {
            Log.e(tag, content);
        }
    }

    public void setupV1Database() {
        deleteDatabase(DB_NAME);
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, 0, null);
        db.setVersion(1);
        db.execSQL("CREATE TABLE todos (id INTEGER PRIMARY KEY, note TEXT NOT NULL)");
        db.execSQL("CREATE INDEX index_note_on_todos ON todos (note)");
        db.execSQL("INSERT INTO todos (note) values ('todo v1 #1'), ('todo v1 #2')");
        db.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setupViews();

        // OrmaDatabase with migration steps
        // The current database schema version is 10 (= BuildConfig.VERSION_CODE)
        setupV1Database();
        orma = OrmaDatabase.builder(this)
                .name(DB_NAME)
                .migrationStep(5, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        // In schema version 5, the table name was changed:
                        helper.renameTable("todos", "Todo");
                    }
                })
                .migrationStep(6, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        // In schema version 7, "note" was renamed to "title":
                        helper.renameColumn("Todo", "note", "title");
                    }
                })
                .migrationStep(7, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        // In schema version 7, "content", "done" were added:
                        helper.execSQL("ALTER TABLE Todo ADD COLUMN content TEXT NULL");
                        helper.execSQL("ALTER TABLE Todo ADD COLUMN done INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .migrationTraceListener(new TraceListener() {
                    @Override
                    public void onTrace(@NonNull final MigrationEngine engine, @NonNull final String format,
                            @NonNull final Object[] args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logsAdapter.addAll(String.format(Locale.getDefault(), format, args));
                                TraceListener.LOGCAT.onTrace(engine, format, args);
                            }
                        });
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

                    largeLogE(TAG, Log.getStackTraceString(e));
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

    void setupViews() {
        logsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        binding.appBarMain.contentMain.listLogs.setAdapter(logsAdapter);

        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);

        FloatingActionButton fab = binding.appBarMain.fab;
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

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);

        binding.appBarMain.contentMain.textInfo.setText(
                "This is Android Orma v" + BuildConfig.VERSION_NAME + ".");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
