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

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.annotation.Experimental;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.rx.RxRelation;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;

import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * A helper class that provides adapter class details.
 *
 * @param <Model> An Orma model class
 */
@Experimental
public class OrmaAdapter<Model> {

    public static final int CACHE_SIZE = BuildConfig.DEBUG ? 2 : 256;

    protected final Context context;

    protected final RxRelation<Model, ?> relation;

    protected final LruCache<Integer, Model> cache = new LruCache<>(CACHE_SIZE);

    protected final Observable<Selector<Model, ?>> queryObservable;

    protected final CompositeDisposable queryObservableSubscription;

    public OrmaAdapter(@NonNull Context context, @NonNull RxRelation<Model, ?> relation) {
        this.context = context;
        this.relation = relation;
        this.queryObservable = relation.createQueryObservable();

        queryObservableSubscription = new CompositeDisposable();
        queryObservableSubscription.add(queryObservable.subscribe(new Consumer<Selector<Model, ?>>() {
            @Override
            public void accept(Selector<Model, ?> models) throws Exception {
                cache.evictAll();
            }
        }));
    }

    @NonNull
    public Observable<Selector<Model, ?>> getQueryObservable() {
        return queryObservable;
    }

    public void addSubscription(Disposable subscription) {
        queryObservableSubscription.add(subscription);
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

    public Callable<Model> createFactory(final @NonNull Model model) {
        return new Callable<Model>() {
            @Override
            public Model call() throws Exception {
                return model;
            }
        };
    }

    @NonNull
    public Model getItem(int position) throws NoValueException {
        if (position >= getItemCount()) {
            throw new NoValueException(
                    "ouf of range: getItem(" + position + ") for the relation with " + getItemCount() + " items");
        }
        Model item = cache.get(position);
        if (item == null) {
            item = relation.get(position);
            cache.put(position, item);
        }
        return item;
    }

    @CheckResult
    @NonNull
    public Single<Model> getItemAsSingle(int position) {
        return relation.getAsSingle(position);
    }

    @CheckResult
    @NonNull
    public Single<Long> addItemAsSingle(Callable<Model> factory) {
        return relation.insertAsSingle(factory);
    }

    @CheckResult
    @NonNull
    public Maybe<Integer> removeItemAsMaybe(@NonNull final Model item) {
        return relation.deleteAsMaybe(item);
    }

    @CheckResult
    @NonNull
    public Single<Integer> clearAsSingle() {
        return relation.deleter().executeAsSingle();
    }
}
