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
package com.github.gfx.android.orma;

import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.exception.InsertionFailureException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;

/**
 * Represents a prepared statement to insert models in batch.
 */
public class Inserter<Model> {

    final OrmaConnection conn;

    final Schema<Model> schema;

    final boolean withoutAutoId;

    final SQLiteStatement statement;

    final String sql;

    public Inserter(OrmaConnection conn, Schema<Model> schema, @OnConflict int onConflictAlgorithm, boolean withoutAutoId) {
        SQLiteDatabase db = conn.getWritableDatabase();
        this.conn = conn;
        this.schema = schema;
        this.withoutAutoId = withoutAutoId;
        sql = schema.getInsertStatement(onConflictAlgorithm, withoutAutoId);
        statement = db.compileStatement(sql);
    }

    public Inserter(OrmaConnection conn, Schema<Model> schema) {
        this(conn, schema, OnConflict.NONE, true);
    }

    /**
     * <p>Inserts {@code model} into a table. Ths method does not modify the {@code model} even if a new row id is given to
     * it.</p>
     *
     * @param model a model object to insert
     * @return The last inserted row id
     */
    public long execute(@NonNull Model model) {
        if (conn.trace) {
            conn.trace(sql, schema.convertToArgs(conn, model, withoutAutoId));
        }
        schema.bindArgs(conn, statement, model, withoutAutoId);
        return statement.executeInsert();
    }

    /**
     * @param modelFactory A mode factory to create a model object to insert
     * @return The last inserted row id
     */
    public long execute(@NonNull Callable<Model> modelFactory) {
        try {
            return execute(modelFactory.call());
        } catch (Exception e) {
            throw new InsertionFailureException(e);
        }
    }

    public void executeAll(@NonNull Iterable<Model> models) {
        for (Model model : models) {
            execute(model);
        }
    }

    /**
     * {@link Single} wrapper to {@code execute(Model)}
     *
     * @param model A model object to insert
     * @return An {@link Observable} for the last inserted row id
     */
    @CheckResult
    @NonNull
    public Single<Long> executeAsObservable(@NonNull final Model model) {
        return Single.create(new Single.OnSubscribe<Long>() {
            @Override
            public void call(SingleSubscriber<? super Long> subscriber) {
                long rowId = execute(model);
                subscriber.onSuccess(rowId);
            }
        });
    }

    /**
     * {@link io.reactivex.Single} wrapper to {@code execute(Model)}
     *
     * @param model A model object to insert
     * @return An {@link io.reactivex.Single} for the last inserted row id
     */
    @CheckResult
    @NonNull
    public io.reactivex.Single<Long> executeAsSingle2(@NonNull final Model model) {
        return io.reactivex.Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return execute(model);
            }
        });
    }

    /**
     * {@link Single} wrapper to {@code execute(ModelFactory<Model>)}.
     * {@link ModelFactory#call()} is called in {@link Single.OnSubscribe#call(Object)}.
     *
     * @param modelFactory A model factory
     * @return An {@link Observable} for the last inserted row id
     */
    @CheckResult
    @NonNull
    public Single<Long> executeAsObservable(@NonNull final ModelFactory<Model> modelFactory) {
        return Single.create(new Single.OnSubscribe<Long>() {
            @Override
            public void call(SingleSubscriber<? super Long> subscriber) {
                long rowId = execute(modelFactory);
                subscriber.onSuccess(rowId);
            }
        });
    }

    /**
     * {@link Single} wrapper to {@code execute(ModelFactory<Model>)}.
     * {@link ModelFactory#call()} is called in {@link Single.OnSubscribe#call(Object)}.
     *
     * @param modelFactory A model factory
     * @return An {@link Observable} for the last inserted row id
     */
    @CheckResult
    @NonNull
    public io.reactivex.Single<Long> executeAsSingle2(@NonNull final Callable<Model> modelFactory) {
        return io.reactivex.Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return execute(modelFactory);
            }
        });
    }

    /**
     * {@link Single} wrapper to {@code execute(Model)}
     *
     * @param models model objects to insert
     * @return An {@link Observable} for the last inserted row ids
     */
    @CheckResult
    @NonNull
    public Observable<Long> executeAllAsObservable(@NonNull final Iterable<Model> models) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                for (Model model : models) {
                    long rowId = execute(model);
                    subscriber.onNext(rowId);
                }
                subscriber.onCompleted();
            }
        });
    }

    /**
     * {@link io.reactivex.Observable} wrapper to {@code execute(Iterable<Model>)}
     *
     * @param models model objects to insert
     * @return An {@link io.reactivex.Observable} for the last inserted row ids
     */
    @CheckResult
    @NonNull
    public io.reactivex.Observable<Long> executeAllAsObservable2(@NonNull final Iterable<Model> models) {
        return io.reactivex.Observable.create(new ObservableOnSubscribe<Long>() {
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
