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

import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.exception.NoValueException;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Single;

/**
 * A helper class that provides adapter class details.
 *
 * @param <Model> An Orma model class
 */
public class OrmaAdapter<Model> {

    final Context context;

    final Relation<Model, ?> relation;

    final Handler handler = new Handler(Looper.getMainLooper());

    public OrmaAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this.context = context;
        this.relation = relation;
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
        return relation.count();
    }

    @NonNull
    public Relation<Model, ?> getRelation() {
        return relation.clone();
    }

    public void runOnUiThread(@NonNull final Runnable task) {
        handler.postDelayed(task, 1000 / 30);
    }

    public Callable<Model> createFactory(final @NonNull Model model) {
        return new Callable<Model>() {
            @Override
            public Model call() throws Exception {
                return model;
            }
        };
    }

    @NonNull
    public Model getItem(int position) {
        if (position >= getItemCount()) {
            throw new NoValueException(
                    "ouf of range: getItem(" + position + ") for the relation with " + getItemCount() + " items");
        }
        return relation.get(position);
    }

    @CheckResult
    @NonNull
    public Single<Model> getItemAsObservable(int position) {
        return relation.getAsObservable(position);
    }

    @CheckResult
    @NonNull
    public io.reactivex.Single<Model> getItemAsSingle2(int position) {
        return relation.getAsSingle2(position);
    }

    @CheckResult
    @NonNull
    public Single<Long> addItemAsObservable(Callable<Model> factory) {
        return relation.insertAsObservable(factory);
    }

    @CheckResult
    @NonNull
    public io.reactivex.Single<Long> addItemAsSingle2(Callable<Model> factory) {
        return relation.insertAsSingle2(factory);
    }

    @CheckResult
    @NonNull
    public Observable<Integer> removeItemAsObservable(@NonNull final Model item) {
        return relation.deleteAsObservable(item);
    }

    @CheckResult
    @NonNull
    public io.reactivex.Maybe<Integer> removeItemAsMaybe2(@NonNull final Model item) {
        return relation.deleteAsMaybe2(item);
    }

    @CheckResult
    @NonNull
    public Single<Integer> clearAsObservable() {
        return relation.deleter().executeAsObservable();
    }

    @CheckResult
    @NonNull
    public io.reactivex.Single<Integer> clearAsSingle2() {
        return relation.deleter().executeAsSingle2();
    }
}
