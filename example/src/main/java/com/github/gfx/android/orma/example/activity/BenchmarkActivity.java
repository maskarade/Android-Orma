package com.github.gfx.android.orma.example.activity;

import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityBenchmarkBinding;
import com.github.gfx.android.orma.example.databinding.ItemResultBinding;
import com.github.gfx.android.orma.example.dbflow.BenchmarkDatabase;
import com.github.gfx.android.orma.example.dbflow.FlowTodo;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.example.orma.Todo_Relation;
import com.github.gfx.android.orma.example.realm.RealmTodo;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action0;
import rx.functions.Action1;
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
        Realm.deleteRealm(realmConf);
        realm = Realm.getInstance(realmConf);
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                orma = OrmaDatabase.builder(BenchmarkActivity.this)
                        .name("orma-benchmark.db")
                        .readOnMainThread(AccessThreadConstraint.NONE)
                        .writeOnMainThread(AccessThreadConstraint.NONE)
                        .writeAheadLogging(false)
                        .build();
                orma.getConnection().resetDatabase();
            }
        });
        FlowManager.getDatabase("Benchmark").reset(this);
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
        new Delete().from(FlowTodo.class).query();

        orma.deleteFromTodo()
                .observable()
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
                        return startInsertWithDBFlow();
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
                        return startSelectAllWithDBFlow();
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

                orma.transactionSync(new TransactionTask() {
                    @Override
                    public void execute() throws Exception {
                        long now = System.currentTimeMillis();

                        Inserter<Todo> statement = orma.prepareInsertIntoTodo();

                        for (int i = 0; i < N; i++) {
                            Todo todo = new Todo();

                            todo.title = titlePrefix + i;
                            todo.content = contentPrefix + i;
                            todo.createdTimeMillis = now;

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
                            todo.setCreatedTimeMillis(now);
                        }
                    }
                });

                subscriber.onSuccess(new Result("Realm/insert", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startInsertWithDBFlow() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();

                TransactionManager.transact(BenchmarkDatabase.NAME, new Runnable() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();

                        for (int i = 1; i <= N; i++) {
                            FlowTodo todo = new FlowTodo();
                            todo.title = titlePrefix + i;
                            todo.content = contentPrefix + i;
                            todo.createdTimeMillis = now;
                            todo.insert();
                        }
                    }
                });

                subscriber.onSuccess(new Result("DBFlow/insert", System.currentTimeMillis() - t0));
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

                Todo_Relation todos = orma.selectFromTodo();
                todos.forEach(new Action1<Todo>() {
                    @Override
                    public void call(Todo todo) {
                        String title = todo.title;
                        String content = todo.content;
                        count.incrementAndGet();
                    }
                });

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

                RealmResults<RealmTodo> results = realm.allObjects(RealmTodo.class);
                for (@SuppressWarnings("unused") RealmTodo todo : results) {
                    String title = todo.getTitle();
                    String content = todo.getContent();
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

    Single<Result> startSelectAllWithDBFlow() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> subscriber) {
                long t0 = System.currentTimeMillis();
                AtomicInteger count = new AtomicInteger();

                From<FlowTodo> rel = new Select().from(FlowTodo.class);
                FlowCursorList<FlowTodo> list = rel.queryCursorList();
                for (int i = 0, size = list.getCount(); i < size; i++) {
                    FlowTodo todo = list.getItem(i);
                    String title = todo.title;
                    String content = todo.content;
                    count.incrementAndGet();
                }
                list.close();

                long dbCount = new Select().count().from(FlowTodo.class).count();
                if (dbCount != count.get()) {
                    throw new AssertionError("unexpected value: " + count.get() + " != " + dbCount);
                }

                Log.d(TAG, "DBFlow/forEachAll count: " + count);
                subscriber.onSuccess(new Result("DBFlow/forEachAll", System.currentTimeMillis() - t0));
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
            ItemResultBinding binding = ItemResultBinding.inflate(getLayoutInflater(), parent, false);

            Result result = getItem(position);
            binding.title.setText(result.title);
            binding.elapsed.setText(result.elapsedMillis + "ms");

            long qps = (long) (TimeUnit.SECONDS.toMillis(1) / (result.elapsedMillis / (double) N));
            binding.qps.setText(qps + "qps");

            return binding.getRoot();
        }
    }
}
