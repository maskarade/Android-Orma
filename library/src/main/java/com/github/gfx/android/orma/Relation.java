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
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.content.ContentValues;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of a relation, or a {@code SELECT} query.
 *
 * @param <Model> An Orma model
 * @param <R>     The derived class itself. e.g {@code class Foo_Schema extends Relation<Foo, Foo_Schema>}
 */
public abstract class Relation<Model, R extends Relation<Model, ?>> extends OrmaConditionBase<Model, R>
        implements Cloneable, Iterable<Model> {

    final protected ArrayList<OrderSpec<Model>> orderSpecs = new ArrayList<>();

    public Relation(@NonNull OrmaConnection connection) {
        super(connection);
    }

    public Relation(@NonNull Relation<Model, ?> relation) {
        super(relation);
        orderSpecs.addAll(relation.orderSpecs);
    }

    @NonNull
    @Override
    protected String buildColumnName(@NonNull ColumnDef<Model, ?> column) {
        return column.getQualifiedName();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public R orderBy(@NonNull OrderSpec<Model> orderSpec) {
        orderSpecs.add(orderSpec);
        return (R) this;
    }

    @Nullable
    protected String buildOrderingTerms() {
        if (orderSpecs.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (OrderSpec<Model> orderSpec : orderSpecs) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(orderSpec);
        }
        return sb.toString();
    }

    @IntRange(from = 0)
    public int count() {
        return selector().count();
    }

    public boolean isEmpty() {
        return selector().isEmpty();
    }

    @NonNull
    public Model get(@IntRange(from = 0) int position) {
        return selector().get(position);
    }

    @NonNull
    public Model getOrCreate(@IntRange(from = 0) long position, @NonNull ModelFactory<Model> factory) {
        Model model = selector().getOrNull(position);
        if (model == null) {
            return conn.createModel(getSchema(), factory);
        } else {
            return model;
        }
    }

    /**
     * Finds the index of the item, assuming an order specified by a set of {@code orderBy*()} methods.
     *
     * @param item The item to find
     * @return The position of the item
     */
    @SuppressWarnings("unchecked")
    public int indexOf(@NonNull Model item) {
        Selector<Model, ?> selector = selector();
        for (OrderSpec<Model> orderSpec : orderSpecs) {
            ColumnDef<Model, ?> column = orderSpec.column;
            if (orderSpec.ordering.equals(OrderSpec.ASC)) {
                selector.where(column, "<", column.getSerialized(item));
            } else {
                selector.where(column, ">", column.getSerialized(item));
            }
        }
        return selector.count();
    }

    @Override
    public abstract Relation<Model, R> clone();

    /**
     * {@code selector()} creates a {@link Selector} with queries that the relation has.
     * For example, if the relation has some order-by queries, the new selector also has them.
     *
     * @return A selector derived from the relation
     */
    @NonNull
    public abstract Selector<Model, ?> selector();

    /**
     * {@code updater()} creates an {@link Updater} with queries that the relation has.
     * For example, if the relation has some order-by queries, the new updater also has them.
     *
     * @return An updater derived from the relation
     */
    @NonNull
    public abstract Updater<Model, ?> updater();

    /**
     * {@code deleter()} creates a {@link Deleter} with queries that the relation has.
     * For example, if the relation has some order-by queries, the new deleter also has them.
     *
     * @return A deleter derived from the relation
     */
    @NonNull
    public abstract Deleter<Model, ?> deleter();

    /**
     * {@code inserter()} creates an {@link Inserter}, a prepared statement for {@code INSERT}.
     *
     * @return An inserter
     */
    @NonNull
    public Inserter<Model> inserter() {
        return inserter(OnConflict.NONE, true);
    }


    /**
     * {@code inserter()} creates an {@link Inserter} with options, a prepared statement for {@code INSERT}.
     *
     * @param onConflictAlgorithm {@link OnConflict} algorithm
     * @return An inserter with options
     */
    @NonNull
    public Inserter<Model> inserter(@OnConflict int onConflictAlgorithm) {
        return new Inserter<>(conn, getSchema(), onConflictAlgorithm, true);
    }

    /**
     * {@code inserter()} creates an {@link Inserter} with options, a prepared statement for {@code INSERT}.
     *
     * @param onConflictAlgorithm {@link OnConflict} algorithm
     * @param withoutAutoId       If {@code true}, the primary key with {@link PrimaryKey#auto()} is omitted in the {@code
     *                            INSERT} statement.
     * @return An {@link Inserter} instance, or a prepared statement to {@code INSERT}.
     */
    @NonNull
    public Inserter<Model> inserter(@OnConflict int onConflictAlgorithm, boolean withoutAutoId) {
        return new Inserter<>(conn, getSchema(), onConflictAlgorithm, withoutAutoId);
    }

    /**
     * Deprecated. Use {@code relation.inserter(OnConflict.REPLACE, false)} or {@code upsert(model)}.
     *
     * @return An {@code Inserter} instance to upsert rows.
     */
    @NonNull
    @Deprecated
    public Inserter<Model> upserter() {
        return inserter(OnConflict.REPLACE, false);
    }

    /**
     * <p>Upsert a model recursively.</p>
     * <p>
     * NOTE: <strong>You must use the return value for the model</strong>
     * because the returned model might have newly assigned the primary key and foreign keys.
     * </p>
     *
     * @param model A model to upsert
     * @return A new model
     */
    @NonNull
    public Model upsert(@NonNull final Model model) {
        class Ref<T> {

            T value;
        }
        final Ref<Model> modelRef = new Ref<>();
        conn.transactionSync(new Runnable() {
            @Override
            public void run() {
                modelRef.value = upsertWithoutTransaction(model);
            }
        });
        return modelRef.value;
    }

    @NonNull
    public List<Model> upsert(@NonNull final Iterable<Model> models) {
        final List<Model> result = new ArrayList<>();
        conn.transactionSync(new Runnable() {
            @Override
            public void run() {
                for (Model model : models) {
                    Model newModel = upsertWithoutTransaction(model);
                    result.add(newModel);
                }
            }
        });
        return result;
    }

    @NonNull
    public abstract Model upsertWithoutTransaction(@NonNull final Model model);

    /**
     * Convert a model to {@code ContentValues}. You can use the content values for raw INSERT and/or UPDATE queries.
     * @param model A model to convert.
     * @param withoutAutoId If true, the autoId is omitted from the return value.
     * @return
     */
    @NonNull
    public ContentValues convertToContentValues(@NonNull Model model, boolean withoutAutoId) {
        return getSchema().convertToContentValues(conn, model, withoutAutoId);
    }

    /**
     * Convert a model to {@code Object[]}, This is similar to {@link #convertToContentValues(Object, boolean)},
     * except for the return value is an array of {@code Object}.
     * @param model A model to convert.
     * @param withoutAutoId If true, the autoId is omitted from the return value.
     * @return
     */
    @NonNull
    public Object[] convertToArgs(@NonNull Model model, boolean withoutAutoId) {
        return getSchema().convertToArgs(conn, model, withoutAutoId);
    }

    // Iterator<Model>

    @Override
    public Iterator<Model> iterator() {
        return selector().iterator();
    }
}
