package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public class HasOne<T> {
    public static <T> HasOne<T> just(long id, T model) {
        return new HasOne<>(id, model);
    }

    final long id;

    final Single<T> single;

    public HasOne(long id, T model) {
        this.id = id;
        this.single = Single.just(model);
    }

    public HasOne(long id, Single<T> single) {
        this.id = id;
        this.single = single;
    }

    public HasOne(@NonNull final OrmaConnection conn, @NonNull final Schema<T> schema, final long id) {
        this.id = id;
        single = Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(SingleSubscriber<? super T> singleSubscriber) {
                ColumnDef<?> primaryKey = schema.getPrimaryKey();
                assert primaryKey != null;
                String whereClause = "\"" + primaryKey.name + "\" = ?";
                String[] whereArgs = {String.valueOf(id)};
                T model = conn.querySingle(schema, schema.getColumnNames(), whereClause, whereArgs, null, null, null);
                if (model != null) {
                    singleSubscriber.onSuccess(model);
                } else {
                    singleSubscriber.onError(new NoValueException("No value found for "
                            + schema.getTableName() + "." + primaryKey.name + " = " + id));
                }
            }
        });
    }

    public long getId() {
        return id;
    }

    @NonNull
    public Single<T> single() {
        return single;
    }
}
