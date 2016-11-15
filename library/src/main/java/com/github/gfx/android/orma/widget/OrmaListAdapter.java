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
import com.github.gfx.android.orma.Selector;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A kind of {@link android.widget.ArrayAdapter} or {@link android.widget.CursorAdapter}.
 */
public abstract class OrmaListAdapter<Model> extends BaseAdapter {

    protected final OrmaAdapter<Model> delegate;

    private final Observable<Selector<Model, ?>> observable;

    public OrmaListAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this(new OrmaAdapter<>(context, relation));
    }

    public OrmaListAdapter(OrmaAdapter<Model> delegate) {
        this.delegate = delegate;

        observable = delegate.getRelation().createQueryObservable();
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Selector<Model, ?>>() {
                    @Override
                    public void accept(Selector<Model, ?> selector) throws Exception {
                        notifyDataSetChanged();
                    }
                });
    }

    @Override
    public int getCount() {
        return delegate.getItemCount();
    }

    @Override
    @NonNull
    public Model getItem(int position) {
        return delegate.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    public Context getContext() {
        return delegate.getContext();
    }

    @NonNull
    public LayoutInflater getLayoutInflater() {
        return delegate.getLayoutInflater();
    }

    @NonNull
    public Relation<Model, ?> getRelation() {
        return delegate.getRelation();
    }

    /**
     * Inserts a model into the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param factory A model factory invoked in a background thread.
     * @return It yields the new position of the item.
     */
    @CheckResult
    @NonNull
    public Single<Long> addItemAsSingle(final Callable<Model> factory) {
        return delegate.addItemAsSingle(factory);
    }

    /**
     * Inserts a model into the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param item A model factory invoked in a background thread.
     * @return It yields the new position of the item.
     */
    @CheckResult
    @NonNull
    public Single<Long> addItemAsSingle(Model item) {
        return addItemAsSingle(delegate.createFactory(item));
    }

    /**
     * Removes an item from the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param item A model to remove.
     * @return A cold {@link Maybe} that yields the position at which the item was. {@code onSuccess()} is only called if the
     * item existed.
     */
    @CheckResult
    @NonNull
    public Maybe<Integer> removeItemAsMaybe(@NonNull final Model item) {
        return delegate.removeItemAsMaybe(item);
    }

    /**
     * Deletes all the rows in the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @return It yields the number of rows deleted.
     */
    @CheckResult
    @NonNull
    public Single<Integer> clearAsSingle() {
        return delegate.clearAsSingle();
    }
}
