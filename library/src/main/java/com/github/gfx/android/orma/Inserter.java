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
import com.github.gfx.android.orma.core.Database;
import com.github.gfx.android.orma.core.DatabaseStatement;
import com.github.gfx.android.orma.event.DataSetChangedEvent;
import com.github.gfx.android.orma.exception.InsertionFailureException;

import android.support.annotation.NonNull;

import java.io.Closeable;
import java.util.concurrent.Callable;

/**
 * Represents a prepared statement to insert models in batch.
 */
public class Inserter<Model> implements Closeable {

    final OrmaConnection conn;

    final Schema<Model> schema;

    final boolean withoutAutoId;

    final DatabaseStatement statement;

    final String sql;

    public Inserter(OrmaConnection conn, Schema<Model> schema, @OnConflict int onConflictAlgorithm, boolean withoutAutoId) {
        Database db = conn.getWritableDatabase();
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
     * <p>Inserts {@code model} into a table. This method does not modify the {@code model} even if a new row id is given to
     * it.</p>
     *
     * @param model a model object to insert
     * @return The last inserted row id. {@code -1} for failure (e.g. constraint violations).
     */
    public long execute(@NonNull Model model) {
        if (conn.trace) {
            conn.trace(sql, schema.convertToArgs(conn, model, withoutAutoId));
        }
        schema.bindArgs(conn, statement, model, withoutAutoId);
        long rowId = statement.executeInsert();
        conn.trigger(DataSetChangedEvent.Type.INSERT, schema);
        return rowId;
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
     * Does {@link #execute(Object)} and then does {@link #close()} immediately
     *
     * @param model A model to insert
     * @return A last-inserted row id
     */
    public long executeAndClose(@NonNull Model model) {
        try {
            return execute(model);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        statement.close();
    }
}
