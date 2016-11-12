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

import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.example.databinding.FragmentBenchmarkBinding;
import com.github.gfx.android.orma.example.databinding.ItemResultBinding;
import com.github.gfx.android.orma.example.handwritten.HandWrittenOpenHelper;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.example.orma.Todo_Selector;
import com.github.gfx.android.orma.example.realm.RealmTodo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class BenchmarkFragment extends Fragment {

    static final String TAG = BenchmarkFragment.class.getSimpleName();

    static final int N_ITEMS = 10;

    static final int N_OPS = 100;

    final String titlePrefix = "title ";

    final String contentPrefix = "content content content\n"
            + "content content content\n"
            + "content content content\n"
            + " ";

    OrmaDatabase orma;

    HandWrittenOpenHelper hw;

    FragmentBenchmarkBinding binding;

    ResultAdapter adapter;

    public BenchmarkFragment() {
    }

    public static Fragment newInstance() {
        return new BenchmarkFragment();
    }

    static long longForQuery(SQLiteDatabase db, String sql, String[] args) {
        Cursor cursor = db.rawQuery(sql, args);
        cursor.moveToFirst();
        long value = cursor.getLong(0);
        cursor.close();
        return value;
    }

    static long runWithBenchmark(Runnable task) {
        long t0 = System.nanoTime();

        for (int i = 0; i < N_OPS; i++) {
            task.run();
        }

        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBenchmarkBinding.inflate(inflater, container, false);

        adapter = new ResultAdapter(getContext());
        binding.list.setAdapter(adapter);

        binding.run.setOnClickListener(v -> run());

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        Realm.init(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        RealmConfiguration realmConf = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConf);
        Realm.deleteRealm(realmConf);

        Schedulers.io().createWorker().schedule(() -> {
            getContext().deleteDatabase("orma-benchmark.db");
            orma = OrmaDatabase.builder(getContext())
                    .name("orma-benchmark.db")
                    .readOnMainThread(AccessThreadConstraint.NONE)
                    .writeOnMainThread(AccessThreadConstraint.NONE)
                    .trace(false)
                    .build();
            orma.migrate();
        });

        getContext().deleteDatabase("hand-written.db");
        hw = new HandWrittenOpenHelper(getContext(), "hand-written.db");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    void run() {
        Log.d(TAG, "Start performing a set of benchmarks");

        adapter.clear();

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> r.delete(RealmTodo.class));
        realm.close();

        hw.getWritableDatabase().execSQL("DELETE FROM todo");

        orma.deleteFromTodo()
                .executeAsSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(integer -> startInsertWithOrma())
                .flatMap(result -> {
                    adapter.add(result);
                    return startInsertWithRealm(); // Realm objects can only be accessed on the thread they were created.
                })
                .flatMap(result -> {
                    adapter.add(result);
                    return startInsertWithHandWritten();
                })
                .flatMap(result -> {
                    adapter.add(result);
                    return startSelectAllWithOrma();
                })
                .flatMap(result -> {
                    adapter.add(result);
                    return startSelectAllWithRealm(); // Realm objects can only be accessed on the thread they were created.
                })
                .flatMap(result -> {
                    adapter.add(result);
                    return startSelectAllWithHandWritten();
                })
                .subscribe(
                        result -> adapter.add(result),
                        error -> {
                            Log.wtf(TAG, error);
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();

                        });
    }

    Single<Result> startInsertWithOrma() {
        return Single.fromCallable(() -> {
            long result = runWithBenchmark(() -> {
                orma.transactionSync(() -> {
                    long now = System.currentTimeMillis();

                    Inserter<Todo> statement = orma.prepareInsertIntoTodo();

                    for (int i = 0; i < N_ITEMS; i++) {
                        Todo todo = new Todo();

                        todo.title = titlePrefix + i;
                        todo.content = contentPrefix + i;
                        todo.createdTime = new Date(now);

                        statement.execute(todo);
                    }
                });
            });
            return new Result("Orma/insert", result);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startInsertWithRealm() {
        return Single.fromCallable(() -> {
            long result = runWithBenchmark(() -> {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(r -> {
                    long now = System.currentTimeMillis();

                    for (int i = 0; i < N_ITEMS; i++) {
                        RealmTodo todo = r.createObject(RealmTodo.class);

                        todo.setTitle(titlePrefix + i);
                        todo.setContent(contentPrefix + i);
                        todo.setCreatedTime(new Date(now));
                    }
                });
                realm.close();
            });
            return new Result("Realm/insert", result);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startInsertWithHandWritten() {
        return Single.fromCallable(() -> {
            long result = runWithBenchmark(() -> {
                SQLiteDatabase db = hw.getWritableDatabase();
                db.beginTransaction();

                SQLiteStatement inserter = db.compileStatement(
                        "INSERT INTO todo (title, content, done, createdTime) VALUES (?, ?, ?, ?)");

                long now = System.currentTimeMillis();

                for (int i = 1; i <= N_ITEMS; i++) {
                    inserter.bindAllArgsAsStrings(new String[]{
                            titlePrefix + i, // title
                            contentPrefix + i, // content
                            "0", // done
                            String.valueOf(now), // createdTime
                    });
                    inserter.executeInsert();
                }

                db.setTransactionSuccessful();
                db.endTransaction();
            });
            return new Result("HandWritten/insert", result);

        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startSelectAllWithOrma() {
        return Single.fromCallable(() -> {
            long result = runWithBenchmark(() -> {
                final AtomicInteger count = new AtomicInteger();

                Todo_Selector todos = orma.selectFromTodo().orderByCreatedTimeAsc();

                for (Todo todo : todos) {
                    @SuppressWarnings("unused")
                    String title = todo.title;
                    @SuppressWarnings("unused")
                    String content = todo.content;
                    @SuppressWarnings("unused")
                    Date createdTime = todo.createdTime;

                    count.incrementAndGet();
                }

                if (todos.count() != count.get()) {
                    throw new AssertionError("unexpected get: " + count.get());
                }
                Log.d(TAG, "Orma/forEachAll count: " + count);
            });
            return new Result("Orma/forEachAll", result);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startSelectAllWithRealm() {
        return Single.fromCallable(() -> {
            long result = runWithBenchmark(() -> {
                AtomicInteger count = new AtomicInteger();
                Realm realm = Realm.getDefaultInstance();
                RealmResults<RealmTodo> results = realm.where(RealmTodo.class)
                        .findAllSorted("createdTime", Sort.ASCENDING);
                for (RealmTodo todo : results) {
                    @SuppressWarnings("unused")
                    String title = todo.getTitle();
                    @SuppressWarnings("unused")
                    String content = todo.getContent();
                    @SuppressWarnings("unused")
                    Date createdTime = todo.getCreatedTime();

                    count.incrementAndGet();
                }
                if (results.size() != count.get()) {
                    throw new AssertionError("unexpected get: " + count.get());
                }
                realm.close();

                Log.d(TAG, "Realm/forEachAll count: " + count);
            });
            return new Result("Realm/forEachAll", result);
        });
    }

    Single<Result> startSelectAllWithHandWritten() {
        return Single.fromCallable(() -> {
            long result = runWithBenchmark(() -> {
                AtomicInteger count = new AtomicInteger();

                SQLiteDatabase db = hw.getReadableDatabase();
                Cursor cursor = db.query(
                        "todo",
                        new String[]{"id, title, content, done, createdTime"},
                        null, null, null, null, "createdTime ASC" // whereClause, whereArgs, groupBy, having, orderBy
                );

                if (cursor.moveToFirst()) {
                    int titleIndex = cursor.getColumnIndexOrThrow("title");
                    int contentIndex = cursor.getColumnIndexOrThrow("content");
                    int createdTimeIndex = cursor.getColumnIndexOrThrow("createdTime");
                    do {
                        @SuppressWarnings("unused")
                        String title = cursor.getString(titleIndex);
                        @SuppressWarnings("unused")
                        String content = cursor.getString(contentIndex);
                        @SuppressWarnings("unused")
                        Date createdTime = new Date(cursor.getLong(createdTimeIndex));

                        count.incrementAndGet();
                    } while (cursor.moveToNext());
                }
                cursor.close();

                long dbCount = longForQuery(db, "SELECT COUNT(*) FROM todo", null);
                if (dbCount != count.get()) {
                    throw new AssertionError("unexpected get: " + count.get() + " != " + dbCount);
                }

                Log.d(TAG, "HandWritten/forEachAll count: " + count);
            });
            return new Result("HandWritten/forEachAll", result);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    static class Result {

        final String title;

        final long elapsedMillis;

        public Result(String title, long elapsedMillis) {
            this.title = title;
            this.elapsedMillis = elapsedMillis;
        }
    }

    static class ResultAdapter extends ArrayAdapter<Result> {

        public ResultAdapter(Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            @SuppressLint("ViewHolder") ItemResultBinding binding = ItemResultBinding
                    .inflate(LayoutInflater.from(getContext()), parent, false);

            Result result = getItem(position);
            assert result != null;
            binding.title.setText(result.title);
            binding.elapsed.setText(result.elapsedMillis + "ms");

            return binding.getRoot();
        }
    }

}
