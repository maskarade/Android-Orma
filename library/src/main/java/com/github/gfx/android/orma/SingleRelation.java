package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.NoValueException;

import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public class SingleRelation<T> {

    final long id;

    final Single<T> single;

    public SingleRelation(long id, T model) {
        this.id = id;
        this.single = Single.just(model);
    }

    public SingleRelation(long id, Single<T> single) {
        this.id = id;
        this.single = single;
    }

    public SingleRelation(@NonNull final OrmaConnection conn, @NonNull final Schema<T> schema, final long id) {
        this.id = id;
        single = Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(SingleSubscriber<? super T> singleSubscriber) {
                ColumnDef<?> primaryKey = schema.getPrimaryKey();
                assert primaryKey != null;
                String whereClause = "\"" + primaryKey.name + "\" = ?";
                String[] whereArgs = {String.valueOf(id)};
                T model = conn.querySingle(schema, schema.getEscapedColumnNames(), whereClause, whereArgs, null, null, null);
                if (model != null) {
                    singleSubscriber.onSuccess(model);
                } else {
                    singleSubscriber.onError(new NoValueException("No value found for "
                            + schema.getTableName() + "." + primaryKey.name + " = " + id));
                }
            }
        });
    }

    public static <T> SingleRelation<T> just(long id, T model) {
        return new SingleRelation<>(id, model);
    }

    public static <T> SingleRelation<T> id(final long id) {
        return new SingleRelation<>(id, Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(SingleSubscriber<? super T> singleSubscriber) {
                singleSubscriber.onError(new NoValueException("No value set for id=" + id));
            }
        }));
    }

    public long getId() {
        return id;
    }

    @NonNull
    public Single<T> single() {
        return single;
    }
}
