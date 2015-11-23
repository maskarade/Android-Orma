package com.github.gfx.android.orma;

import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public class Updater<T, C extends Updater<?, ?>> extends OrmaConditionBase<T, C> {

    final protected ContentValues contents = new ContentValues();

    public Updater(OrmaConnection conn, Schema<T> schema) {
        super(conn, schema);
    }

    @NonNull
    public ContentValues getContentValues() {
        return contents;
    }

    /**
     * @return The number of rows updated.
     */
    public int execute() {
        return conn.update(schema.getTableName(), contents, getWhereClause(), getWhereArgs());
    }

    public Single<Integer> observable() {
        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(SingleSubscriber<? super Integer> subscriber) {
                subscriber.onSuccess(execute());
            }
        });
    }
}
