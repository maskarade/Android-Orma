package com.github.gfx.android.orma.example.activity;

import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityTodoBinding;
import com.github.gfx.android.orma.example.databinding.CardTodoBinding;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class TodoActivity extends AppCompatActivity {

    public static Intent createIntent(Context context) {
        return new Intent(context, TodoActivity.class);
    }

    OrmaDatabase orma;

    ActivityTodoBinding binding;

    Adapter adapter;

    int number = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_todo);

        orma = new OrmaDatabase(this, "main.db");

        adapter = new Adapter();
        binding.list.setAdapter(adapter);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Todo todo = new Todo();
                number++;
                todo.title = "todo #" + number;
                todo.content = "content #" + number;
                todo.createdTimeMillis = System.currentTimeMillis();
                adapter.addItem(todo);
            }
        });
    }

    class VH extends RecyclerView.ViewHolder {

        CardTodoBinding binding;

        public VH(ViewGroup parent) {
            super(CardTodoBinding.inflate(getLayoutInflater(), parent, false).getRoot());
            binding = DataBindingUtil.getBinding(itemView);
        }
    }

    class Adapter extends RecyclerView.Adapter<VH> {

        int count = 0;

        List<Todo> items;

        Adapter() {
            Observable<Integer> countObservable = orma.selectFromTodo()
                    .countAsObservable()
                    .subscribeOn(Schedulers.io());

            Observable<List<Todo>> itemsObservable = orma.selectFromTodo()
                    .observable()
                    .subscribeOn(Schedulers.io())
                    .toList();

            Observable.combineLatest(countObservable, itemsObservable, new Func2<Integer, List<Todo>, Pair<Integer, List<Todo>>>() {
                @Override
                public Pair<Integer, List<Todo>> call(Integer count, List<Todo> todos) {
                    return Pair.create(count, todos);
                }
            })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Pair<Integer, List<Todo>>>() {
                        @Override
                        public void call(Pair<Integer, List<Todo>> pair) {
                            count = pair.first;
                            items = new ArrayList<>(pair.second);
                            notifyDataSetChanged();
                        }
                    });
        }

        public void addItem(final Todo todo) {
            items.add(todo);

            orma.transactionAsync(new TransactionTask() {
                @Override
                public void execute() throws Exception {
                    orma.prepareInsertIntoTodo()
                            .execute(todo);
                }
            });

            count++;

            notifyItemInserted(count);
        }

        public void removeItem(Todo todo) {
            orma.deleteFromTodo()
                    .where("id = ?", todo.id)
                    .observable()
                    .subscribeOn(Schedulers.io())
                    .subscribe();

            int position = items.indexOf(todo);
            if (position == -1) {
                throw new AssertionError("something is wrong");
            }
            items.remove(todo);
            count--;
            notifyItemRemoved(position);
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(parent);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            CardTodoBinding binding = holder.binding;
            final Todo todo = items.get(position);

            binding.title.setText(todo.title);
            binding.content.setText(todo.content);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(todo);
                }
            });
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }
}
