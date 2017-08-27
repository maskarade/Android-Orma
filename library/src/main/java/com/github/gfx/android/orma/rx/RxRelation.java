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

import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.annotation.Experimental;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.event.DataSetChangedEvent;

import android.support.annotation.CheckResult;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

/**
 * Representation of a relation, or a {@code SELECT} query with RxJava support.
 *
 * @param <Model> An Orma model
 * @param <R>     The derived class itself. e.g {@code class Foo_Schema extends RxRelation<Foo, Foo_Schema>}
 */
public abstract class RxRelation<Model, R extends RxRelation<Model, ?>> extends Relation<Model, R> {

    protected final RxOrmaConnection conn;

    public RxRelation(@NonNull RxOrmaConnection connection) {
        super(connection);
        conn = connection;
    }

    public RxRelation(@NonNull RxRelation<Model, ?> relation) {
        super(relation);
        conn = relation.conn;
    }

    @CheckResult
    @NonNull
    public Single<Model> getAsSingle(@IntRange(from = 0) final int position) {
        return Single.fromCallable(new Callable<Model>() {
            @Override
            public Model call() throws Exception {
                return get(position);
            }
        });
    }

    /**
     * Deletes a specified model and yields where it was. Suitable to implement {@link android.widget.Adapter}.
     * Operations are executed in a transaction.
     *
     * @param item A model to delete.
     * @return An {@link Maybe} that yields the position of the deleted item if the item is deleted.
     */
    @CheckResult
    @NonNull
    public Maybe<Integer> deleteAsMaybe(@NonNull final Model item) {
        return Maybe.create(new MaybeOnSubscribe<Integer>() {
            @Override
            public void subscribe(MaybeEmitter<Integer> emitter) throws Exception {
                final AtomicInteger positionRef = new AtomicInteger(-1);
                conn.transactionSync(new Runnable() {
                    @Override
                    public void run() {
                        int position = indexOf(item);
                        ColumnDef<Model, ?> pk = getSchema().getPrimaryKey();
                        int deletedRows = deleter()
                                .where(pk, "=", pk.getSerialized(item))
                                .execute();

                        if (deletedRows > 0) {
                            positionRef.set(position);
                        }
                    }
                });
                // emit the position *after* the transaction finished
                if (positionRef.get() >= 0) {
                    emitter.onSuccess(positionRef.get());
                } else {
                    emitter.onComplete();
                }
            }
        });
    }

    /**
     * Truncates the table to the specified size.
     *
     * @param size Size to truncate the table
     * @return A {@link Single} that yields the number of rows deleted.
     */
    @CheckResult
    @NonNull
    public Single<Integer> truncateAsSingle(@IntRange(from = 0) final int size) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                String pk = getSchema().getPrimaryKey().getEscapedName();
                Selector<Model, ?> subquery = selector();
                subquery.limit(Integer.MAX_VALUE);
                subquery.offset(size);
                return conn.delete(getSchema(), pk + " IN (" + subquery.buildQueryWithColumns(pk) + ")", getBindArgs());
            }
        });
    }

    /**
     * Inserts an item.
     *
     * @param factory A model to insert.
     * @return An {@link Single} that yields the newly inserted row id.
     */
    @CheckResult
    @NonNull
    public Single<Long> insertAsSingle(@NonNull final Callable<Model> factory) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return inserter().execute(factory);
            }
        });
    }

    /**
     * {@code selector()} creates a {@link RxSelector} with queries that the relation has.
     * For example, if the relation has some order-by queries, the new selector also has them.
     *
     * @return A selector derived from the relation
     */
    @NonNull
    @Override
    public abstract RxSelector<Model, ?> selector();

    /**
     * {@code updater()} creates an {@link RxUpdater} with queries that the relation has.
     * For example, if the relation has some order-by queries, the new updater also has them.
     *
     * @return An updater derived from the relation
     */
    @NonNull
    @Override
    public abstract RxUpdater<Model, ?> updater();

    /**
     * {@code deleter()} creates a {@link RxDeleter} with queries that the relation has.
     * For example, if the relation has some order-by queries, the new deleter also has them.
     *
     * @return A deleter derived from the relation
     */
    @NonNull
    @Override
    public abstract RxDeleter<Model, ?> deleter();

    /**
     * {@code inserter()} creates an {@link RxInserter}, a prepared statement for {@code INSERT}.
     *
     * @return An inserter
     */
    @NonNull
    @Override
    public RxInserter<Model> inserter() {
        return inserter(OnConflict.NONE, true);
    }

    /**
     * {@code inserter()} creates an {@link RxInserter} with options, a prepared statement for {@code INSERT}.
     *
     * @param onConflictAlgorithm {@link OnConflict} algorithm
     * @return An inserter with options
     */
    @NonNull
    @Override
    public RxInserter<Model> inserter(@OnConflict int onConflictAlgorithm) {
        return new RxInserter<>(conn, getSchema(), onConflictAlgorithm, true);
    }

    /**
     * {@code inserter()} creates an {@link RxInserter} with options, a prepared statement for {@code INSERT}.
     *
     * @param onConflictAlgorithm {@link OnConflict} algorithm
     * @param withoutAutoId       If {@code true}, the primary key with {@link PrimaryKey#auto()} is omitted in the {@code
     *                            INSERT} statement.
     * @return An {@link Inserter} instance, or a prepared statement to {@code INSERT}.
     */
    @NonNull
    @Override
    public RxInserter<Model> inserter(@OnConflict int onConflictAlgorithm, boolean withoutAutoId) {
        return new RxInserter<>(conn, getSchema(), onConflictAlgorithm, withoutAutoId);
    }

    /**
     * Deprecated. Use {@code relation.inserter(OnConflict.REPLACE, false)} or {@code upsert(model)}.
     *
     * @return An {@code RxInserter} instance to upsert rows.
     */
    @NonNull
    @Override
    @Deprecated
    public RxInserter<Model> upserter() {
        return inserter(OnConflict.REPLACE, false);
    }

    /**
     * RxJava interface to {@link #upsert(Object)}.
     *
     * @param model A model to upsert
     * @return An observable that yields a new model.
     */
    @NonNull
    @CheckResult
    public Single<Model> upsertAsSingle(@NonNull final Model model) {
        return Single.fromCallable(new ModelFactory<Model>() {
            @NonNull
            @Override
            public Model call() {
                return upsert(model);
            }
        });
    }

    @NonNull
    @CheckResult
    public Observable<Model> upsertAsObservable(@NonNull final Iterable<Model> models) {
        return Observable.create(new ObservableOnSubscribe<Model>() {
            @Override
            public void subscribe(final ObservableEmitter<Model> emitter) {
                conn.transactionSync(new Runnable() {
                    @Override
                    public void run() {
                        for (Model model : models) {
                            Model newModel = upsertWithoutTransaction(model);
                            emitter.onNext(newModel);
                        }
                    }
                });
                emitter.onComplete();
            }
        });
    }

    /**
     * Experimental API to observe data-set changed events.
     *
     * @param <S> A concrete {@link Selector} class.
     * @return A hot observable that yields {@link Selector} when the target data-set is changed.
     */
    @Experimental
    @SuppressWarnings("unchecked")
    public <S extends Selector<Model, ?>> Observable<S> createQueryObservable() {
        return conn.createEventObservable((S) selector())
                .map(new Function<DataSetChangedEvent<S>, S>() {
                    @Override
                    public S apply(DataSetChangedEvent<S> event) throws Exception {
                        return event.getSelector();
                    }
                });
    }

    /**
     * Experimental API to observe data-set changed events.
     * This is provided to test whether it is useful or not, and not intended to be used in production yet.
     *
     * @param <S> A concrete {@link Selector} class.
     * @return A hot observable that yields {@link Selector} when the target data-set is changed.
     */
    @Experimental
    @Deprecated
    @SuppressWarnings("unchecked")
    public <S extends Selector<Model, ?>> Observable<DataSetChangedEvent<S>> createEventObservable() {
        return conn.createEventObservable((S) selector());
    }
}
