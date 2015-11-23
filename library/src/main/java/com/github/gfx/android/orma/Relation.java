package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.InvalidStatementException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public abstract class Relation<T, R extends Relation<?, ?>> extends OrmaConditionBase<T, R> {

    @Nullable
    protected String groupBy;

    @Nullable
    protected String having;

    @Nullable
    protected String orderBy;

    protected long limit = -1;

    protected long offset = -1;

    public Relation(@NonNull OrmaConnection connection, @NonNull Schema<T> schema) {
        super(connection, schema);
    }

    @SuppressWarnings("unchecked")
    public R groupBy(String groupBy) {
        this.groupBy = groupBy;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R having(String having) {
        this.having = having;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R orderBy(String... orderBys) {
        this.orderBy = TextUtils.join(", ", orderBys);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R limit(long limit) {
        this.limit = limit;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R offset(long offset) {
        this.offset = offset;
        return (R) this;
    }

    @Nullable
    private String getLimitClause() {
        if (limit != -1) {
            if (offset != -1) {
                return limit + "," + offset;
            } else {
                return String.valueOf(limit);
            }
        } else {
            if (offset != -1) {
                throw new InvalidStatementException("Missing limit()");
            } else {
                return null;
            }
        }
    }

    public long count() {
        return conn.count(schema.getTableName(), getWhereClause(), getWhereArgs());
    }

    @Nullable
    public T valueOrNull() {
        return conn.querySingle(schema, schema.getEscapedColumnNames(),
                getWhereClause(), getWhereArgs(), groupBy, having, orderBy);
    }

    @NonNull
    public T value() throws NoValueException {
        T model = valueOrNull();

        if (model == null) {
            throw new NoValueException("Expected single value but nothing for " + schema.getTableName());
        }

        return model;
    }

    @NonNull
    public List<T> toList() {
        final ArrayList<T> list = new ArrayList<>();

        forEach(new Action1<T>() {
            @Override
            public void call(T item) {
                list.add(item);
            }
        });

        return list;
    }

    public void forEach(@NonNull Action1<T> action) {
        Cursor cursor = conn.query(schema.getTableName(), schema.getEscapedColumnNames(), getWhereClause(),
                getWhereArgs(), groupBy, having, orderBy, getLimitClause());

        if (cursor.moveToFirst()) {
            do {
                action.call(schema.createModelFromCursor(conn, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @NonNull
    public Observable<T> observable() {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                // FIXME: error handling, run-on-background

                forEach(new Action1<T>() {
                    @Override
                    public void call(T item) {
                        subscriber.onNext(item);
                    }
                });

                subscriber.onCompleted();
            }
        });
    }
}
