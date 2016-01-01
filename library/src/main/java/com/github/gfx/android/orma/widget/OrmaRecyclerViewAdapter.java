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

package com.github.gfx.android.orma.widget;

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.Relation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class OrmaRecyclerViewAdapter<Model, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    final Context context;

    int totalCount = 0;

    final Relation<Model, ?> relation;

    final Handler handler = new Handler(Looper.getMainLooper());

    public OrmaRecyclerViewAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this.context = context;
        this.relation = relation;
        totalCount = relation.selector().count();
    }

    public Context getContext() {
        return context;
    }

    public Relation<Model, ?> getRelation() {
        return relation;
    }

    public void runOnMainThread(@NonNull Runnable task) {
        if (handler.getLooper().getThread() == Thread.currentThread()) {
            task.run();
        } else {
            handler.post(task);
        }
    }

    @NonNull
    public Model getItem(int position) {
        return relation.get(position);
    }

    public void addItem(final ModelFactory<Model> item) {
        relation.inserter()
                .observable(item)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                totalCount++;
                                notifyItemInserted(totalCount);
                            }
                        });
                    }
                });
    }

    public void addItem(final Model item) {
        addItem(new ModelFactory<Model>() {
            @Override
            public Model call() {
                return item;
            }
        });
    }

    public void removeItem(@NonNull final Model todo) {
        relation.deleteAsObservable(todo)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(final Integer position) {
                        if (position != -1) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    totalCount--;
                                    notifyItemRemoved(position);
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return totalCount;
    }

}
