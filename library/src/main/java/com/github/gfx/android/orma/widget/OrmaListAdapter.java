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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import rx.Observable;
import rx.Single;
import rx.functions.Action1;

public abstract class OrmaListAdapter<Model> extends BaseAdapter {

    protected final OrmaAdapterDelegate<Model> delegate;

    public OrmaListAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this(new OrmaAdapterDelegate<Model>(context, relation));
    }

    public OrmaListAdapter(OrmaAdapterDelegate<Model> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getCount() {
        return delegate.totalCount;
    }

    @Override
    @NonNull
    public Model getItem(int position) {
        return delegate.getItem(position);
    }

    @NonNull
    public Context getContext() {
        return delegate.getContext();
    }

    @NonNull
    public LayoutInflater getLayoutInflater() {
        return delegate.getLayoutInflater();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public <T extends Relation<Model, ?>> Relation<Model, T> getRelation() {
        return delegate.getRelation();
    }

    /**
     * Same as {@link android.app.Activity#runOnUiThread(Runnable)}.
     * @param task A task to run on the UI thread.
     */
    public void runOnUiThread(@NonNull Runnable task) {
        delegate.runOnUiThread(task);
    }

    /**
     * Inserts a model into the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param factory A model factory invoked in a background thread.
     * @return A hot {@link Observable} that yields the new position of the item.
     */
    @NonNull
    public Single<Long> addItemAsObservable(final ModelFactory<Model> factory) {
        return delegate.addItemAsObservable(factory)
                .doOnSuccess(new Action1<Long>() {
                    @Override
                    public void call(Long rowId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    /**
     * Inserts a model into the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param item A model factory invoked in a background thread.
     * @return A hot {@link Observable} that yields the new position of the item.
     */
    public Single<Long> addItemAsObservable(final Model item) {
        return addItemAsObservable(new ModelFactory<Model>() {
            @Override
            public Model call() {
                return item;
            }
        });
    }

    /**
     * Removes an item from the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param item A model to remove.
     * @return A hot {@link Observable} that yields the position at which the item was. {@code onNext()} is only called if the
     * item existed.
     */
    public Observable<Integer> removeItemAsObservable(@NonNull final Model item) {
        return delegate.removeItemAsObservable(item)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer position) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    /**
     * Deletes all the rows in the table and invokes {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @return A hot {@link Observable} that yields the new {@code totalCount()} (i.e. always {@code 0}).
     */
    public Single<Integer> clearAsObservable() {
        return delegate.clearAsObservable()
                .doOnSuccess(new Action1<Integer>() {
                    @Override
                    public void call(Integer deletedItems) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                });
    }
}
