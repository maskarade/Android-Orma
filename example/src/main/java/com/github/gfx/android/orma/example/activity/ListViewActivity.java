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
import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityListViewBinding;
import com.github.gfx.android.orma.example.databinding.CardTodoBinding;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.widget.OrmaListViewAdapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import rx.schedulers.Schedulers;

public class ListViewActivity extends AppCompatActivity {

    OrmaDatabase orma;

    ActivityListViewBinding binding;

    Adapter adapter;

    int number = 0;

    public static Intent createIntent(Context context) {
        return new Intent(context, ListViewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_view);

        orma = OrmaDatabase.builder(this)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();

        adapter = new Adapter(this, orma.relationOfTodo().orderByCreatedTimeMillisAsc());
        binding.list.setAdapter(adapter);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addItemAsObservable(new ModelFactory<Todo>() {
                    @Override
                    public Todo call() {
                        Todo todo = new Todo();
                        number++;
                        todo.title = "todo #" + number;
                        todo.content = "content #" + number;
                        todo.createdTimeMillis = System.currentTimeMillis();
                        return todo;
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });
    }

    static class Adapter extends OrmaListViewAdapter<Todo> {

        public Adapter(@NonNull Context context, @NonNull Relation<Todo, ?> relation) {
            super(context, relation);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = CardTodoBinding.inflate(getLayoutInflater(), parent, false).getRoot();
            }

            final Todo todo = getItem(position);
            CardTodoBinding binding = DataBindingUtil.getBinding(convertView);

            binding.title.setText(todo.title);
            binding.content.setText(todo.content);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItemAsObservable(todo)
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                }
            });

            return convertView;
        }
    }

}
