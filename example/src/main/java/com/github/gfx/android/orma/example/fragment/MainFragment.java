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

package com.github.gfx.android.orma.example.fragment;

import com.github.gfx.android.orma.core.Database;
import com.github.gfx.android.orma.core.DefaultDatabase;
import com.github.gfx.android.orma.encryption.EncryptedDatabase;
import com.github.gfx.android.orma.example.BuildConfig;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.activity.MainActivity;
import com.github.gfx.android.orma.example.databinding.FragmentMainBinding;
import com.github.gfx.android.orma.example.orma.Category;
import com.github.gfx.android.orma.example.orma.Item;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.example.tool.LargeLog;
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.TraceListener;

import org.threeten.bp.ZonedDateTime;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Locale;

import io.reactivex.Single;

public class MainFragment extends Fragment {

    static final String TAG = MainActivity.class.getSimpleName();

    static final String DB_NAME = "main.db";

    static final String PASSWORD = "password";

    ArrayAdapter<String> logsAdapter;

    FragmentMainBinding binding;

    OrmaDatabase orma;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public void setupV1Database() {
        getContext().deleteDatabase(DB_NAME);
        Database db;
        if (BuildConfig.FLAVOR.equals("encrypted")) {
            db = new EncryptedDatabase.Provider(PASSWORD).provide(getContext(), DB_NAME, 0);
        } else {
            db = new DefaultDatabase.Provider().provide(getContext(), DB_NAME, 0);
        }
        db.setVersion(1);
        db.execSQL("CREATE TABLE todos (id INTEGER PRIMARY KEY, note TEXT NOT NULL)");
        db.execSQL("CREATE INDEX index_note_on_todos ON todos (note)");
        db.execSQL("INSERT INTO todos (note) values ('todo v1 #1'), ('todo v1 #2')");
        db.close();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        setupViews();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        // OrmaDatabase with migration steps
        // The current database schema version is 10 (= BuildConfig.VERSION_CODE)
        setupV1Database();
        OrmaDatabase.Builder builder = OrmaDatabase.builder(getContext()).name(DB_NAME);
        if (BuildConfig.FLAVOR.equals("encrypted")) {
            builder = builder.provider(new EncryptedDatabase.Provider(PASSWORD));
        }
        orma = builder
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
                .migrationTraceListener((engine, format, args) ->
                        binding.getRoot().post(() -> {
                            logsAdapter.addAll(String.format(Locale.getDefault(), format, args));
                            TraceListener.LOGCAT.onTrace(engine, format, args);
                        }))
                .build();

        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            try {
                Thread.sleep(1000);

                orma.migrate(); // may throws SQLiteConstraintException

                Log.d(TAG, "CRUD:start ------------------------");
                simpleCrud();
                Log.d(TAG, "rxCRUD:start ------------------------");
                rxCrud();
                Log.d(TAG, "CRUD:start ------------------------");
                associations();
                Log.d(TAG, "------------------------");
            } catch (final Exception e) {
                binding.getRoot().post(() -> {
                    LargeLog.e(TAG, e);
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Demonstrates simple CRUD operations, which makes no sense though.
     */
    void simpleCrud() {
        // create
        orma.insertIntoTodo(Todo.create("buy", "milk banana apple"));

        // read
        for (Todo todo : orma.selectFromTodo().titleEq("buy")) {
            Log.d(TAG, "selectFromTodo: " + todo.id);
        }

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

    @SuppressWarnings("CheckReturnValue")
    void rxCrud() {
        // create
        orma.prepareInsertIntoTodoAsSingle()
                .flatMapObservable(todoInserter -> Single.concat(
                        todoInserter.executeAsSingle(Todo.create("today", "coffee")),
                        todoInserter.executeAsSingle(Todo.create("tomorrow", "tea"))
                ).toObservable())
                .subscribe((rowId) -> {
                    Log.d(TAG, "inserted: " + rowId);
                });

        // read
        orma.selectFromTodo()
                .executeAsObservable()
                .subscribe((item) -> {
                    Log.d(TAG, "rx select: " + item.title);
                });

        // update
        orma.updateTodo()
                .titleEq("today")
                .done(true)
                .executeAsSingle()
                .subscribe((count) -> {
                    Log.d(TAG, "updated count: " + count);
                });

        // delete
        orma.deleteFromTodo()
                .doneEq(true)
                .executeAsSingle()
                .subscribe((count) -> {
                    Log.d(TAG, "deleted count: " + count);
                });
    }

    void associations() {
        orma.deleteFromCategory().execute();
        orma.deleteFromItem().execute();

        Category category = orma.relationOfCategory().getOrCreate(0, () -> new Category("foo"));

        Item item = category.createItem(orma, ZonedDateTime.now().toString());
        Log.d(TAG, "created: " + item);

        Log.d(TAG, "A category has many items (" + category.getItems(orma).count() + ")");
        Log.d(TAG, TextUtils.join(", ", category.getItems(orma)));
    }

    void setupViews() {
        logsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        binding.listLogs.setAdapter(logsAdapter);

        binding.textInfo.setText(
                "This is Android Orma v" + BuildConfig.VERSION_NAME + ".");
    }
}
