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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import rx.Observable;
import rx.Single;
import rx.functions.Action1;


/**
 * A {@link RecyclerView.Adapter} with the Orma backend, just like {@link android.widget.CursorAdapter}.
 *
 * @param <Model> An Orma model class
 * @param <VH>    A concrete view holder class
 */
public abstract class OrmaRecyclerViewAdapter<Model, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected final OrmaAdapterDelegate<Model> delegate;

    public OrmaRecyclerViewAdapter(@NonNull Context context, @NonNull Relation<Model, ?> relation) {
        this(new OrmaAdapterDelegate<>(context, relation));
    }

    public OrmaRecyclerViewAdapter(@NonNull OrmaAdapterDelegate<Model> delegate) {
        this.delegate = delegate;
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

    @SuppressWarnings("unchecked")
    @NonNull
    public <T extends Relation<Model, ?>> Relation<Model, T> getRelation() {
        return delegate.getRelation();
    }

    public void runOnUiThreadSync(@NonNull Runnable task) {
        delegate.runOnUiThreadSync(task);
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
    @NonNull
    public Single<Long> addItemAsObservable(@NonNull final ModelFactory<Model> factory) {
        return delegate.addItemAsObservable(factory)
                .doOnSuccess(new Action1<Long>() {
                    @Override
                    public void call(Long rowId) {
                        runOnUiThreadSync(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemInserted(getItemCount());
                            }
                        });
                    }
                });
    }

    /**
     * Inserts a model into the table and invokes {@link RecyclerView.Adapter#notifyItemInserted(int)}
     *
     * @param item A model factory invoked in a background thread.
     * @return A hot {@link Observable} that yields the newly inserted row id.
     */
    public Single<Long> addItemAsObservable(@NonNull final Model item) {
        return addItemAsObservable(new ModelFactory<Model>() {
            @Override
            public Model call() {
                return item;
            }
        });
    }

    /**
     * Removes an item from the table and invokes {@link RecyclerView.Adapter#notifyItemRemoved(int)}.
     *
     * @param item A model to remove.
     * @return An {@link Observable} that yields the position at which the item was. {@code onNext()} is only called if the
     * item existed.
     */
    public Observable<Integer> removeItem(@NonNull final Model item) {
        return delegate.removeItemAsObservable(item)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer position) {
                        runOnUiThreadSync(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemRemoved(position);
                            }
                        });
                    }
                });
    }

    /**
     * Deletes all the rows in the table and invokes {@link RecyclerView.Adapter#notifyDataSetChanged()}.
     *
     * @return A {@link Single} that yields the number of deleted items.
     */
    public Single<Integer> clearAsObservable() {
        return delegate.clearAsObservable()
                .doOnSuccess(new Action1<Integer>() {
                    @Override
                    public void call(Integer deletedItems) {
                        runOnUiThreadSync(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                });
    }
}
