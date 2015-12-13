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

import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.github.gfx.android.orma.AccessThreadConstraint;
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
import android.view.View;
import android.view.ViewGroup;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class TodoActivity extends AppCompatActivity {

    OrmaDatabase orma;

    ActivityTodoBinding binding;

    Adapter adapter;

    int number = 0;

    public static Intent createIntent(Context context) {
        return new Intent(context, TodoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_todo);

        orma = OrmaDatabase.builder(this)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();

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

        int totalCount = 0;

        Adapter() {
            totalCount = orma.selectFromTodo().count();
        }

        public void addItem(final Todo todo) {
            orma.prepareInsertIntoTodo()
                    .observable(todo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            totalCount++;
                            notifyItemInserted(totalCount);
                        }
                    });
        }

        public boolean removeItem(final Todo todo) {
            orma.transactionAsync(new TransactionTask() {
                @Override
                public void execute() throws Exception {
                    final int position = orma.selectFromTodo()
                            .createdTimeMillisLt(todo.createdTimeMillis)
                            .count();
                    final int deletedRows = orma.deleteFromTodo()
                            .idEq(todo.id)
                            .execute();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (deletedRows > 0) {
                                totalCount--;
                                notifyItemRemoved(position);
                            }
                        }
                    });
                }
            });

            return true;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(parent);
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            CardTodoBinding binding = holder.binding;
            final Todo todo = orma.selectFromTodo()
                    .orderByCreatedTimeMillisAsc()
                    .get(position);

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
            return totalCount;
        }
    }
}
