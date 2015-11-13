package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.databinding.ActivityBenchmarkBinding;
import com.github.gfx.android.orma.example.databinding.ItemResultBinding;

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

    OrmaDatabase db;

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

        db = new OrmaDatabase(this, "benchmark.db");
        db.getConnection().resetDatabase();

        RealmConfiguration realmConf = new RealmConfiguration.Builder(this)
                .build();
        Realm.deleteRealm(realmConf);
        realm = Realm.getInstance(realmConf);

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
    }

    void run() {
        Log.d(TAG, "Start performing a set of benchmarks");

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

                db.transaction(new TransactionTask() {
                    @Override
                    public void execute() throws Exception {
                        for (int i = 0; i < N; i++) {
                            Todo todo = new Todo();

                            todo.title = "title " + i;
                            todo.content = "content " + i;

                            db.insert(todo);
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
                        for (int i = 0; i < N; i++) {
                            RealmTodo todo = realm.createObject(RealmTodo.class);

                            todo.setTitle("title " + i);
                            todo.setContent("content " + i);
                        }
                    }
                });

                singleSubscriber.onSuccess(new Result("Realm/insert", System.currentTimeMillis() - t0));
            }
        });
    }

    Single<Result> startSelectAllWithOrma() {
        return Single.create(new Single.OnSubscribe<Result>() {
            @Override
            public void call(SingleSubscriber<? super Result> singleSubscriber) {
                long t0 = System.currentTimeMillis();
                List<Todo> list = db.fromTodo().toList();
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
