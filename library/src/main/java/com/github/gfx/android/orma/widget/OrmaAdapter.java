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
import android.view.LayoutInflater;

import rx.Observable;
import rx.Single;
import rx.functions.Action1;

/**
 * A helper class to provide Adapter method implementations.
 *
 * @param <Model> An Orma model class
 */
public class OrmaAdapter<Model> {

    final Context context;

    final Relation<Model, ?> relation;

    final Handler handler = new Handler(Looper.getMainLooper());

    int totalCount = 0;

    public OrmaAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this.context = context;
        this.relation = relation;
        totalCount = relation.selector().count();
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @NonNull
    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(context);
    }

    public int getItemCount() {
        return totalCount;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public <T extends Relation<Model, ?>> Relation<Model, T> getRelation() {
        return (Relation<Model, T>) relation;
    }

    public void runOnUiThread(@NonNull final Runnable task) {
        handler.post(task);
    }

    @NonNull
    public Model getItem(int position) {
        return getItemAsObservable(position).toBlocking().value();
    }

    @NonNull
    public Single<Model> getItemAsObservable(int position) {
        return relation.getWithTransactionAsObservable(position);
    }

    @NonNull
    public Single<Long> addItemAsObservable(final ModelFactory<Model> factory) {
        return relation.insertWithTransactionAsObservable(factory)
                .doOnSuccess(new Action1<Long>() {
                    @Override
                    public void call(Long rowId) {
                        totalCount++;
                    }
                });
    }

    @NonNull
    public Observable<Integer> removeItemAsObservable(@NonNull final Model item) {
        return relation.deleteWithTransactionAsObservable(item)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer deletedPosition) {
                        totalCount--;
                    }
                });
    }

    @NonNull
    public Single<Integer> clearAsObservable() {
        return relation.deleter()
                .executeAsObservable()
                .doOnSuccess(new Action1<Integer>() {
                    @Override
                    public void call(Integer deletedItems) {
                        totalCount = 0;
                    }
                });
    }
}
