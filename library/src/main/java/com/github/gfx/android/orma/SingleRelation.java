package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.NoValueException;

import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Represents a has-one relation with lazy loading.
 *
 * @param <Model> The type of a model to relate.
 */
public class SingleRelation<Model> {

    final long id;

    final Single<Model> single;

    public SingleRelation(long id, Model model) {
        this.id = id;
        this.single = Single.just(model);
    }

    public SingleRelation(long id, Single<Model> single) {
        this.id = id;
        this.single = single;
    }

    public SingleRelation(@NonNull final OrmaConnection conn, @NonNull final Schema<Model> schema, final long id) {
        this.id = id;
        single = Single.create(new Single.OnSubscribe<Model>() {
            @Override
            public void call(SingleSubscriber<? super Model> subscriber) {
                ColumnDef<?> primaryKey = schema.getPrimaryKey();
                String whereClause = "\"" + primaryKey.name + "\" = ?";
                String[] whereArgs = {String.valueOf(id)};
                Model model = conn.querySingle(schema, schema.getEscapedColumnNames(),
                        whereClause, whereArgs, null, null, null, 0);
                if (model != null) {
                    subscriber.onSuccess(model);
                } else {
                    subscriber.onError(new NoValueException("No value found for "
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
    public Single<Model> single() {
        return single;
    }
}
