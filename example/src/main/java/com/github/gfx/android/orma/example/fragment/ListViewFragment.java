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

import com.github.gfx.android.orma.example.databinding.CardTodoBinding;
import com.github.gfx.android.orma.example.databinding.FragmentListViewBinding;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.example.orma.Todo_Relation;
import com.github.gfx.android.orma.widget.OrmaListAdapter;

import org.threeten.bp.ZonedDateTime;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ListViewFragment extends Fragment {

    OrmaDatabase orma;

    FragmentListViewBinding binding;

    Adapter adapter;

    int number = 0;

    public ListViewFragment() {
    }

    public static ListViewFragment newInstance() {
        return new ListViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListViewBinding.inflate(inflater, container, false);

        orma = OrmaDatabase.builder(getContext())
                .build();

        adapter = new Adapter(getContext(), orma.relationOfTodo().orderByCreatedTimeAsc());
        binding.list.setAdapter(adapter);

        binding.fab.setOnClickListener(v -> adapter.addItemAsSingle(() -> {
            Todo todo = new Todo();
            number++;
            todo.title = "ListView item #" + number;
            todo.content = ZonedDateTime.now().toString();
            todo.createdTime = new Date();
            return todo;
        })
                .subscribeOn(Schedulers.io())
                .subscribe());

        return binding.getRoot();
    }

    static class Adapter extends OrmaListAdapter<Todo> {

        public Adapter(@NonNull Context context, @NonNull Todo_Relation relation) {
            super(context, relation);
        }

        @NonNull
        @Override
        public Todo_Relation getRelation() {
            return (Todo_Relation) super.getRelation();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = CardTodoBinding.inflate(getLayoutInflater(), parent, false).getRoot();
            }

            final Todo todo = getItem(position);
            final CardTodoBinding binding = DataBindingUtil.getBinding(convertView);

            binding.title.setText(todo.title);
            binding.content.setText(todo.content);

            setStrike(binding.title, todo.done);

            binding.getRoot().setOnClickListener(v -> {
                Todo currentTodo = getRelation().reload(todo);
                final boolean done = !currentTodo.done;

                getRelation()
                        .updater()
                        .idEq(todo.id)
                        .done(done)
                        .executeAsSingle()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> {
                            setStrike(binding.title, done);
                        });
            });

            binding.getRoot().setOnLongClickListener(v -> {
                removeItemAsMaybe(todo)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
                return true;
            });

            return convertView;
        }

        void setStrike(TextView textView, boolean value) {
            if (value) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

}
