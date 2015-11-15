package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.databinding.ActivityBenchmarkBinding;
import com.github.gfx.android.orma.example.databinding.ItemResultBinding;
import com.github.gfx.android.orma.example.dbflow.BenchmarkDatabase;
import com.github.gfx.android.orma.example.dbflow.FlowTodo;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

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

        orma = new OrmaDatabase(this, "benchmark.db");
        orma.getConnection().resetDatabase();

        RealmConfiguration realmConf = new RealmConfiguration.Builder(this)
                .build();
        Realm.deleteRealm(realmConf);
        realm = Realm.getInstance(realmConf);
    }

    @Override
    protected void onPause() {
        super.onPause();

        realm.close();
    }

    void run() {
        Log.d(TAG, "Start performing a set of benchmarks");

        orma.deleteFromTodo().execute();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(RealmTodo.class);
            }
        });
        new Delete().from(FlowTodo.class).query();

        startInsertWithOrma()
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startInsertWithRealm();
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
                        return startSelectAllWithRealm();
                    }
                })
                .flatMap(new Func1<Result, Single<Result>>() {
                    @Override
                    public Single<Result> call(Result result) {
                        adapter.add(result);
                        return startSelectAllWithDBFlow();
                    }
                })
                .subscribe(new Action1<Result>() {
                    @Override
                    public void call(Result result) {
                        adapter.add(result);
                    }
                });
    }

    Single<Result> startInsertWithOrma() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
                long t0 = System.currentTimeMillis();

                orma.transaction(new TransactionTask() {
                    @Override
                    public void execute() throws Exception {
                        long now = System.currentTimeMillis();

                        Inserter<Todo> statement = orma.prepareInsertIntoTodo();

                        for (int i = 0; i < N; i++) {
                            Todo todo = new Todo();

                            todo.title = titlePrefix + i;
                            todo.content = contentPrefix + i;
                            todo.createdTimeMillis = now;

                            statement.insert(todo);
                        }
                    }
                });

                singleSubscriber.onSuccess(new Result("Orma/insert", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startInsertWithRealm() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
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

                singleSubscriber.onSuccess(new Result("Realm/insert", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startInsertWithDBFlow() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
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

                singleSubscriber.onSuccess(new Result("DBFlow/insert", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startSelectAllWithOrma() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
                long t0 = System.currentTimeMillis();
                List<Todo> list = orma.selectFromTodo().toList();
                Log.d(TAG, "Orma/selectAll count: " + list.size());
                singleSubscriber.onSuccess(new Result("Orma/selectAll", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startSelectAllWithRealm() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
                long t0 = System.currentTimeMillis();
                List<RealmTodo> list = realm.allObjects(RealmTodo.class);
                Log.d(TAG, "Realm/selectAll count: " + list.size());
                singleSubscriber.onSuccess(new Result("Realm/selectAll", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startSelectAllWithDBFlow() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
                long t0 = System.currentTimeMillis();

                List<FlowTodo> list = new Select().from(FlowTodo.class).queryList();
                Log.d(TAG, "DBFlow/selectAll count: " + list.size());
                singleSubscriber.onSuccess(new Result("DBFlow/selectAll", System.currentTimeMillis() - t0));
            }
        });
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
