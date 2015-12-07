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
     * @return The last inserted row id.
     */
    public long execute(@NonNull Model model) {
        schema.bindArgs(conn, statement, model);
        return statement.executeInsert();
    }

    public Single<Long> observable(@NonNull final Model model) {
        return Single.create(new Single.OnSubscribe<Long>() {
            @Override
            public void call(SingleSubscriber<? super Long> subscriber) {
                subscriber.onSuccess(execute(model));
            }
        });
    }
}
