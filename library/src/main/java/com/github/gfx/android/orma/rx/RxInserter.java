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

package com.github.gfx.android.orma.rx;

import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.OnConflict;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;

public class RxInserter<Model> extends Inserter<Model> {

    public RxInserter(OrmaConnection conn, Schema<Model> schema, @OnConflict int onConflictAlgorithm, boolean withoutAutoId) {
        super(conn, schema, onConflictAlgorithm, withoutAutoId);
    }

    public RxInserter(OrmaConnection conn, Schema<Model> schema) {
        super(conn, schema);
    }

    /**
     * {@link Single} wrapper to {@code execute(Model)}
     *
     * @param model A model object to insert
     * @return An {@link Single} for the last inserted row id
     */
    @CheckResult
    @NonNull
    public Single<Long> executeAsSingle(@NonNull final Model model) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return execute(model);
            }
        });
    }

    /**
     * {@link Single} wrapper to {@code execute(ModelFactory<Model>)}.
     *
     * @param modelFactory A model factory
     * @return It yields the inserted row id
     */
    @CheckResult
    @NonNull
    public Single<Long> executeAsSingle(@NonNull final Callable<Model> modelFactory) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return execute(modelFactory);
            }
        });
    }

    /**
     * {@link Observable} wrapper to {@code execute(Iterable<Model>)}
     *
     * @param models model objects to insert
     * @return It yields the inserted row ids
     */
    @CheckResult
    @NonNull
    public Observable<Long> executeAllAsObservable(@NonNull final Iterable<Model> models) {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                for (Model model : models) {
                    emitter.onNext(execute(model));
                }
                emitter.onComplete();
            }
        });
    }
}
