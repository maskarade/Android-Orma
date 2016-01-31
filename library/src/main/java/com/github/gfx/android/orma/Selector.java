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
import com.github.gfx.android.orma.internal.OrmaIterator;

import android.database.Cursor;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.FuncN;

public abstract class Selector<Model, S extends Selector<Model, ?>>
        extends OrmaConditionBase<Model, S> implements Iterable<Model>, Cloneable {

    @Nullable
    protected String groupBy;

    @Nullable
    protected String having;

    @Nullable
    protected String orderBy;

    protected long limit = -1;

    protected long offset = -1;

    protected long page = -1;

    public Selector(@NonNull OrmaConnection conn, @NonNull Schema<Model> schema) {
        super(conn, schema);
    }

    public Selector(@NonNull OrmaConditionBase<Model, ?> condition) {
        super(condition);
        if (condition instanceof Relation) {
            @SuppressWarnings("unchecked")
            Relation<Model, ?> relation = (Relation<Model, ?>) condition;
            CharSequence orderByTerm = relation.buildOrderingTerms();
            if (orderByTerm != null) {
                orderBy(orderByTerm);
            }
        }
    }

    @Override
    public abstract S clone();

    @SuppressWarnings("unchecked")
    public S groupBy(@NonNull String groupBy) {
        this.groupBy = groupBy;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S having(@NonNull String having, @NonNull Object... args) {
        this.having = having;
        appendBindArgs(args);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S orderBy(@NonNull CharSequence orderByTerm) {
        if (orderBy == null) {
            orderBy = orderByTerm.toString();
        } else {
            orderBy += ", " + orderByTerm;
        }
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S limit(@IntRange(from = 1, to = Integer.MAX_VALUE) long limit) {
        this.limit = limit;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S offset(@IntRange(from = 0) long offset) {
        this.offset = offset;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S page(@IntRange(from = 1) long page) {
        this.page = page;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S per(@IntRange(from = 1, to = Integer.MAX_VALUE) long per) {
        this.limit = per;
        return (S) this;
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

    /**
     * Provided for {@link Observable#combineLatest(List, FuncN)}.
     *
     * @return An observable that yields {@link #count()}.
     */
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

    public boolean empty() {
        return count() == 0;
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
     *
     * @param action An action called for each model in the iteration.
     */
    public void forEach(@NonNull Action1<Model> action) {
        Cursor cursor = execute();
        for (int pos = 0; cursor.moveToPosition(pos); pos++) {
            action.call(newModelFromCursor(cursor));
        }
        cursor.close();
    }

    @NonNull
    public Model newModelFromCursor(@NonNull Cursor cursor) {
        return schema.newModelFromCursor(conn, cursor);
    }

    @NonNull
    public Observable<Model> executeAsObservable() {
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

    // implements Iterable<Model>

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Iterator<Model> iterator() {
        return new OrmaIterator<>(this);
    }
}
