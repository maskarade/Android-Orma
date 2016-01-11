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
import com.github.gfx.android.orma.exception.OrmaException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A helper class to provide Adapter method implementations.
 *
 * @param <Model> An Orma model class
 */
public class OrmaAdapter<Model> {

    int totalCount = 0;

    final Context context;

    final Relation<Model, ?> relation;

    final Handler handler = new Handler(Looper.getMainLooper());

    final Scheduler background = Schedulers.from(AsyncTask.SERIAL_EXECUTOR);

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

    public void runOnUiThreadSync(@NonNull final Runnable task) {
        if (handler.getLooper().getThread() == Thread.currentThread()) {
            task.run();
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    latch.countDown();
                    task.run();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new OrmaException(e);
            }
        }
    }

    @NonNull
    public Model getItem(int position) {
        return relation.getWithTransactionAsObservable(position)
                .subscribeOn(background)
                .toBlocking()
                .value();
    }

    public Single<Long> addItemAsObservable(final ModelFactory<Model> factory) {
        return relation.insertWithTransactionAsObservable(factory)
                .subscribeOn(background)
                .doOnSuccess(new Action1<Long>() {
                    @Override
                    public void call(Long rowId) {
                        totalCount++;
                    }
                });
    }

    public Observable<Integer> removeItemAsObservable(@NonNull final Model item) {
        return relation.deleteWithTransactionAsObservable(item)
                .subscribeOn(background)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer deletedPosition) {
                        totalCount--;
                    }
                });
    }

    public Single<Integer> clearAsObservable() {
        return relation.deleter()
                .executeAsObservable()
                .subscribeOn(background)
                .doOnSuccess(new Action1<Integer>() {
                    @Override
                    public void call(Integer deletedItems) {
                        totalCount = getItemCount();
                    }
                });
    }
}
