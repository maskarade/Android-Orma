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

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Represents a prepared statement to insert models in batch.
 */
public class Inserter<Model> {

    final OrmaConnection conn;

    final Schema<Model> schema;

    final SQLiteStatement statement;

    public Inserter(OrmaConnection conn, Schema<Model> schema, SQLiteStatement statement) {
        this.conn = conn;
        this.schema = schema;
        this.statement = statement;
    }

    /**
     * <p>Inserts {@code model} into a table. Ths method does not modify the {@code model} even if a new row id is given to
     * it.</p>
     *
     * @param model a model object to insert
     * @return The last inserted row id
     */
    public long execute(@NonNull Model model) {
        schema.bindArgs(conn, statement, model);
        return statement.executeInsert();
    }

    /**
     *
     * @param modelFactory A mode factory to create a model object to insert
     * @return The last inserted row id
     */
    public long execute(@NonNull ModelFactory<Model> modelFactory) {
        schema.bindArgs(conn, statement, modelFactory.create());
        return statement.executeInsert();
    }

    public void executeAll(@NonNull Iterable<Model> models) {
        for (Model model : models) {
            execute(model);
        }
    }

    /**
     * {@link Single} wrapper to {@code execute(Model)}
     * @param model A model object to insert
     * @return A cold observable for the last inserted row id
     */
    public Single<Long> observable(@NonNull final Model model) {
        return Single.create(new Single.OnSubscribe<Long>() {
            @Override
            public void call(SingleSubscriber<? super Long> subscriber) {
                subscriber.onSuccess(execute(model));
            }
        });
    }

    /**
     * {@link Single} wrapper to {@code execute(ModelFactory<Model>)}.
     * {@link ModelFactory#create()} is called in {@link Single.OnSubscribe#call(Object)}.
     *
     * @param modelFactory A model factory
     * @return A cold observable for the last inserted row id
     */
    public Single<Long> observable(@NonNull final ModelFactory<Model> modelFactory) {
        return Single.create(new Single.OnSubscribe<Long>() {
            @Override
            public void call(SingleSubscriber<? super Long> subscriber) {
                subscriber.onSuccess(execute(modelFactory.create()));
            }
        });
    }
}
