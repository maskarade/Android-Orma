package com.github.gfx.android.orma;

import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public class Deleter<T, C extends Deleter<?, ?>> extends OrmaConditionBase<T, C> {

    public Deleter(OrmaConnection connection, Schema<T> schema) {
        super(connection, schema);
    }

    /**
     * @return Number of rows deleted.
     */
    public int execute() {
        return conn.delete(schema, getWhereClause(), getWhereArgs());
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
