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

import com.github.gfx.android.orma.encryption.EncryptedDatabase;
import com.github.gfx.android.orma.example.BuildConfig;
import com.github.gfx.android.orma.example.databinding.CardTodoBinding;
import com.github.gfx.android.orma.example.databinding.FragmentRecyclerViewBinding;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.example.orma.Todo_Relation;
import com.github.gfx.android.orma.rx.RxRelation;
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter;

import org.threeten.bp.ZonedDateTime;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecyclerViewFragment extends Fragment {

    OrmaDatabase orma;

    FragmentRecyclerViewBinding binding;

    Adapter adapter;

    int number = 0;

    public static Fragment newInstance() {
        return new RecyclerViewFragment();
    }

    public RecyclerViewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerViewBinding.inflate(inflater, container, false);

        OrmaDatabase.Builder builder = OrmaDatabase.builder(getContext());
        if (BuildConfig.FLAVOR.equals("encrypted")) {
            builder = builder.provider(new EncryptedDatabase.Provider("password"));
        }
        orma = builder.build();

        adapter = new Adapter(getContext(), orma.relationOfTodo().orderByCreatedTimeAsc());
        binding.list.setAdapter(adapter);

        binding.fab.setOnClickListener(v -> adapter.addItemAsSingle(() -> {
            Todo todo = new Todo();
            number++;
            todo.title = "RecyclerView item #" + number;
            todo.content = ZonedDateTime.now().toString();
            todo.createdTime = new Date();
            return todo;
        })
                .subscribeOn(Schedulers.io())
                .subscribe());

        return binding.getRoot();
    }

    static class VH extends RecyclerView.ViewHolder {

        CardTodoBinding binding;

        public VH(LayoutInflater inflater, ViewGroup parent) {
            super(CardTodoBinding.inflate(inflater, parent, false).getRoot());
            binding = DataBindingUtil.getBinding(itemView);
        }
    }

    static class Adapter extends OrmaRecyclerViewAdapter<Todo, VH> {

        public Adapter(@NonNull Context context, @NonNull RxRelation<Todo, ?> relation) {
            super(context, relation);
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public Todo_Relation getRelation() {
            return (Todo_Relation) super.getRelation();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            final CardTodoBinding binding = holder.binding;
            final Todo todo = getItem(position);

            binding.title.setText(todo.title);
            binding.content.setText(todo.content);
            setStrike(binding.title, todo.done);

            binding.getRoot().setOnClickListener(v -> {
                Todo currentTodo = getRelation().reload(todo);
                final boolean done = !currentTodo.done;

                @SuppressWarnings("unused")
                Disposable disposable = getRelation()
                        .updater()
                        .idEq(todo.id)
                        .done(done)
                        .executeAsSingle()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> {
                            // unlike ListViewAdapter, RecyclerViewAdapter can notify single item changed
                            notifyItemChanged(position);
                        });
            });

            binding.getRoot().setOnLongClickListener(v -> {
                removeItemAsMaybe(todo)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
                return true;
            });
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
