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

import com.github.gfx.android.orma.exception.InvalidStatementException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.database.Cursor;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Representation of a relation, or a {@code SELECT} query.
 *
 * @param <Model> An Orma model
 * @param <R>     The derived class itself. e.g {@code class Foo_Schema extends Relation<Foo, Foo_Schema>}
 */
public abstract class Relation<Model, R extends Relation<?, ?>>
        extends OrmaConditionBase<Model, R> implements Iterable<Model> {

    @Nullable
    protected String groupBy;

    @Nullable
    protected String having;

    @Nullable
    protected String orderBy;

    protected long limit = -1;

    protected long offset = -1;

    protected long page = -1;

    public Relation(@NonNull OrmaConnection connection, @NonNull Schema<Model> schema) {
        super(connection, schema);
    }

    @SuppressWarnings("unchecked")
    public R groupBy(@NonNull String groupBy) {
        this.groupBy = groupBy;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R having(@NonNull String having, @NonNull Object... args) {
        this.having = having;
        appendBindArgs(args);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R orderBy(@NonNull String... orderBys) {
        this.orderBy = TextUtils.join(", ", orderBys);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R limit(@IntRange(from = 1, to = Integer.MAX_VALUE) long limit) {
        this.limit = limit;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R offset(@IntRange(from = 0) long offset) {
        this.offset = offset;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R page(@IntRange(from = 1) long page) {
        this.page = page;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R per(@IntRange(from = 1, to = Integer.MAX_VALUE) long per) {
        this.limit = per;
        return (R) this;
    }

    @Nullable
    private String getLimitClause() {
        if (page != -1 && offset != -1) {
            throw new InvalidStatementException("page() and offset() are exclusive. Use either.");
        }

        if (limit != -1) {
            if (offset != -1) {
                return offset + "," + limit;
            } else if (page != -1) {
                return ((page - 1) * limit) + "," + limit;
            } else {
                return String.valueOf(limit);
            }
        } else { // when limit == -1, offset and page must be -1
            if (offset != -1 || page != -1) {
                throw new InvalidStatementException("Missing limit() when offset() or page() is specified.");
            } else {
                return null;
            }
        }
    }

    @IntRange(from = 0)
    public int count() {
        return conn.count(schema, getWhereClause(), getBindArgs());
    }

    @NonNull
    public Observable<Integer> countAsObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(count());
                subscriber.onCompleted();
            }
        });
    }

    @Nullable
    public Model valueOrNull() {
        return getOrNull(0);
    }

    @NonNull
    public Model value() throws NoValueException {
        Model model = getOrNull(0);
        if (model == null) {
            throw new NoValueException("Expected single value but nothing for " + schema.getTableName());
        }
        return model;
    }

    @Nullable
    public Model getOrNull(@IntRange(from = 0) long position) {
        return conn.querySingle(schema, schema.getEscapedColumnNames(),
                getWhereClause(), getBindArgs(), groupBy, having, orderBy, position);
    }

    @NonNull
    public Model get(@IntRange(from = 0) long position) {
        Model model = getOrNull(position);
        if (model == null) {
            throw new NoValueException("Expected single value for " + position + " but nothing for " + schema.getTableName());
        }
        return model;
    }

    @NonNull
    public Cursor execute() {
        return conn.query(schema, schema.getEscapedColumnNames(),
                getWhereClause(), getBindArgs(), groupBy, having, orderBy, getLimitClause());
    }

    @NonNull
    public Cursor executeWithColumns(@NonNull String... columns) {
        return conn.query(schema, columns,
                getWhereClause(), getBindArgs(), groupBy, having, orderBy, getLimitClause());
    }

    /**
     * Executes a query and returns the result as a list.
     *
     * @return A list of models
     */
    @NonNull
    public List<Model> toList() {
        final ArrayList<Model> list = new ArrayList<>();
        forEach(new Action1<Model>() {
            @Override
            public void call(Model item) {
                list.add(item);
            }
        });
        return list;
    }

    /**
     * Executes a query and calls {@code Action1<Model>#call} for each model}.
     */
    public void forEach(@NonNull Action1<Model> action) {
        Cursor cursor = execute();
        for (int pos = 0; cursor.moveToPosition(pos); pos++) {
            action.call(schema.newModelFromCursor(conn, cursor));
        }
        cursor.close();
    }

    @NonNull
    public Observable<Model> observable() {
        return Observable.create(new Observable.OnSubscribe<Model>() {
            @Override
            public void call(final Subscriber<? super Model> subscriber) {
                forEach(new Action1<Model>() {
                    @Override
                    public void call(Model item) {
                        subscriber.onNext(item);
                    }
                });
                subscriber.onCompleted();
            }
        });
    }

    // Other operation helpers

    @NonNull
    public Inserter<Model> inserter() {
        return inserter(OnConflict.NONE);
    }

    @NonNull
    public Inserter<Model> inserter(@OnConflict int onConflictAlgorithm) {
        return new Inserter<>(conn, schema, schema.getInsertStatement(onConflictAlgorithm));
    }

    @NonNull
    public abstract Updater<Model, ?> updater();

    @NonNull
    public abstract Deleter<Model, ?> deleter();

    // implements Iterable<Model>

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Iterator<Model> iterator() {
        return observable().toBlocking().getIterator();
    }
}
