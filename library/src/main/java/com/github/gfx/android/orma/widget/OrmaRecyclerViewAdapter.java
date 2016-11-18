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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * A {@link RecyclerView.Adapter} with the Orma backend, createFactory like {@link android.widget.CursorAdapter}.
 *
 * @param <Model> An Orma model class
 * @param <VH>    A concrete view holder class
 */
public abstract class OrmaRecyclerViewAdapter<Model, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected final OrmaAdapter<Model> delegate;

    public OrmaRecyclerViewAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this(new OrmaAdapter<>(context, relation));
    }

    @SuppressWarnings("unchecked")
    public OrmaRecyclerViewAdapter(@NonNull OrmaAdapter<Model> delegate) {
        this.delegate = delegate;
        delegate.getQueryObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Selector<Model, ?>>() {
                    @Override
                    public void accept(Selector<Model, ?> selector) throws Exception {
                        notifyDataSetChanged();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return delegate.getItemCount();
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

    @NonNull
    public Model getItem(int position) {
        return delegate.getItem(position);
    }

    /**
     * Inserts a model into the table and invokes {@link RecyclerView.Adapter#notifyItemInserted(int)}
     *
     * @param factory A model factory invoked in a background thread.
     * @return A {@link Single} that yields the newly inserted row id.
     */
    @CheckResult
    @NonNull
    public Single<Long> addItemAsSingle(@NonNull final Callable<Model> factory) {
        return delegate.addItemAsSingle(factory);
    }

    /**
     * Inserts a model into the table and invokes {@link RecyclerView.Adapter#notifyItemInserted(int)}
     *
     * @param item A model factory invoked in a background thread.
     * @return It yields the inserted row id.
     */
    @CheckResult
    @NonNull
    public Single<Long> addItemAsSingle(@NonNull Model item) {
        return addItemAsSingle(delegate.createFactory(item));
    }

    /**
     * Removes an item from the table and invokes {@link RecyclerView.Adapter#notifyItemRemoved(int)}.
     *
     * @param item A model to remove.
     * @return It yields the position at which the item was. {@code onNext()} is only called if the
     * item existed.
     */
    @CheckResult
    @NonNull
    public Maybe<Integer> removeItemAsMaybe(@NonNull Model item) {
        return delegate.removeItemAsMaybe(item);
    }

    /**
     * Deletes all the rows in the table and invokes {@link RecyclerView.Adapter#notifyDataSetChanged()}.
     *
     * @return It yields the number of deleted items.
     */
    @CheckResult
    @NonNull
    public Single<Integer> clearAsSingle() {
        return delegate.clearAsSingle();
    }

}
