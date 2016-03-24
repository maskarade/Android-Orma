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

import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityBenchmarkBinding;
import com.github.gfx.android.orma.example.databinding.ItemResultBinding;
import com.github.gfx.android.orma.example.handwritten.HandWrittenOpenHelper;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.example.orma.Todo_Selector;
import com.github.gfx.android.orma.example.realm.RealmTodo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BenchmarkActivity extends AppCompatActivity {

    static final String TAG = BenchmarkActivity.class.getSimpleName();

    final int N = 10000;

    final String titlePrefix = "title ";

    final String contentPrefix = "content content content\n"
            + "content content content\n"
            + "content content content\n"
            + " ";

    OrmaDatabase orma;

    Realm realm;

    HandWrittenOpenHelper hw;

    ActivityBenchmarkBinding binding;

    ResultAdapter adapter;

    public static Intent createIntent(Context context) {
        return new Intent(context, BenchmarkActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_benchmark);

        adapter = new ResultAdapter();
        binding.list.setAdapter(adapter);

        binding.run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        RealmConfiguration realmConf = new RealmConfiguration.Builder(BenchmarkActivity.this)
                .build();
        Realm.setDefaultConfiguration(realmConf);
        Realm.deleteRealm(realmConf);

        realm = Realm.getDefaultInstance();
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                deleteDatabase("orma-benchmark.db");
                orma = OrmaDatabase.builder(BenchmarkActivity.this)
                        .name("orma-benchmark.db")
                        .readOnMainThread(AccessThreadConstraint.NONE)
                        .writeOnMainThread(AccessThreadConstraint.NONE)
                        .writeAheadLogging(false)
                        .trace(false)
                        .build();
                orma.migrate();
            }
        });

        deleteDatabase("hand-written.db");
        hw = new HandWrittenOpenHelper(this, "hand-written.db");
    }

    @Override
    protected void onPause() {
        super.onPause();

        realm.close();
    }

    void run() {
        Log.d(TAG, "Start performing a set of benchmarks");

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(RealmTodo.class);
            }
        });

        hw.getWritableDatabase().execSQL("DELETE FROM todo");

        orma.deleteFromTodo()
                .executeAsObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Integer, Single<Result>>() {
                    @Override
                    public Single<Result> call(Integer integer) {
                        return startInsertWithOrma();
                    }
                })
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startInsertWithRealm(); // Realm objects can only be accessed on the thread they were created.
                    }
                })
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startInsertWithHandWritten();
                    }
                })
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startSelectAllWithOrma();
                    }
                })
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startSelectAllWithRealm(); // Realm objects can only be accessed on the thread they were created.
                    }
                })
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startSelectAllWithHandWritten();
                    }
                })
                .subscribe(new SingleSubscriber<Result>() {
                    @Override
                    public void onSuccess(Result result) {
                        adapter.add(result);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.wtf(TAG, error);
                        Toast.makeText(BenchmarkActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    Single<Result> startInsertWithOrma() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();

                orma.transactionSync(new Runnable() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();

                        Inserter<Todo> statement = orma.prepareInsertIntoTodo();

                        for (int i = 0; i < N; i++) {
                            Todo todo = new Todo();

                            todo.title = titlePrefix + i;
                            todo.content = contentPrefix + i;
                            todo.createdTime = new Date(now);

                            statement.execute(todo);
                        }
                    }
                });

                subscriber.onSuccess(new Result("Orma/insert", System.currentTimeMillis() - t0));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startInsertWithRealm() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        long now = System.currentTimeMillis();

                        for (int i = 0; i < N; i++) {
                            RealmTodo todo = realm.createObject(RealmTodo.class);

                            todo.setTitle(titlePrefix + i);
                            todo.setContent(contentPrefix + i);
                            todo.setCreatedTime(new Date(now));
                        }
                    }
                });

                subscriber.onSuccess(new Result("Realm/insert", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startInsertWithHandWritten() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();

                SQLiteDatabase db = hw.getWritableDatabase();
                db.beginTransaction();

                SQLiteStatement inserter = db.compileStatement(
                        "INSERT INTO todo (title, content, done, createdTime) VALUES (?, ?, ?, ?)");

                long now = System.currentTimeMillis();

                for (int i = 1; i <= N; i++) {
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

                subscriber.onSuccess(new Result("HandWritten/insert", System.currentTimeMillis() - t0));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startSelectAllWithOrma() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();
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
                    throw new AssertionError("unexpected value: " + count.get());
                }
                Log.d(TAG, "Orma/forEachAll count: " + count);
                subscriber.onSuccess(new Result("Orma/forEachAll", System.currentTimeMillis() - t0));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Single<Result> startSelectAllWithRealm() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();
                AtomicInteger count = new AtomicInteger();

                RealmResults<RealmTodo> results = realm.allObjectsSorted(
                        RealmTodo.class, "createdTime", Sort.ASCENDING);
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
                    throw new AssertionError("unexpected value: " + count.get());
                }

                Log.d(TAG, "Realm/forEachAll count: " + count);
                subscriber.onSuccess(new Result("Realm/forEachAll", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startSelectAllWithHandWritten() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();
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

                long dbCount = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM todo", null);
                if (dbCount != count.get()) {
                    throw new AssertionError("unexpected value: " + count.get() + " != " + dbCount);
                }

                Log.d(TAG, "HandWritten/forEachAll count: " + count);
                subscriber.onSuccess(new Result("HandWritten/forEachAll", System.currentTimeMillis() - t0));
            }
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

    class ResultAdapter extends ArrayAdapter<Result> {

        public ResultAdapter() {
            super(BenchmarkActivity.this, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder") ItemResultBinding binding = ItemResultBinding
                    .inflate(getLayoutInflater(), parent, false);

            Result result = getItem(position);
            binding.title.setText(result.title);
            binding.elapsed.setText(result.elapsedMillis + "ms");

            long qps = (long) (TimeUnit.SECONDS.toMillis(1) / (result.elapsedMillis / (double) N));
            binding.qps.setText(qps + "qps");

            return binding.getRoot();
        }
    }
}
