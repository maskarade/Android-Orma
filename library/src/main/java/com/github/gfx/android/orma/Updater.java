package com.github.gfx.android.orma;

import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public class Updater<Model, U extends Updater<?, ?>> extends OrmaConditionBase<Model, U> {

    final protected ContentValues contents = new ContentValues();

    public Updater(@NonNull OrmaConnection conn, @NonNull Schema<Model> schema) {
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
        return conn.update(schema, contents, getWhereClause(), getWhereArgs());
    }

    @NonNull
    public Single<Integer> observable() {
        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(SingleSubscriber<? super Integer> subscriber) {
                subscriber.onSuccess(execute());
            }
        });
    }
}
