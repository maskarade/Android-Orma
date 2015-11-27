package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.InvalidStatementException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.database.Cursor;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public abstract class Relation<Model, R extends Relation<?, ?>> extends OrmaConditionBase<Model, R> {

    @Nullable
    protected String groupBy;

    @Nullable
    protected String having;

    @Nullable
    protected String orderBy;

    protected long limit = -1;

    protected long offset = -1;

    protected long page = -1;

    public Relation(@NonNull OrmaConnection connection, @NonNull Schema<Model> schema) {
        super(connection, schema);
    }

    @SuppressWarnings("unchecked")
    public R groupBy(@NonNull String groupBy) {
        this.groupBy = groupBy;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R having(@NonNull String having) {
        this.having = having;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R orderBy(@NonNull String... orderBys) {
        this.orderBy = TextUtils.join(", ", orderBys);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R limit(@IntRange(from = 1, to = Integer.MAX_VALUE) long limit) {
        this.limit = limit;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R offset(@IntRange(from = 0, to = Integer.MAX_VALUE) long offset) {
        this.offset = offset;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R page(@IntRange(from = 1, to = Integer.MAX_VALUE) long page) {
        this.page = page;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R per(@IntRange(from = 1, to = Integer.MAX_VALUE) long per) {
        this.limit = per;
        return (R) this;
    }

    @Nullable
    private String getLimitClause() {
        if (page != -1 && offset != -1) {
            throw new InvalidStatementException("page() and offset() are exclusive. Use either.");
        }

        if (limit != -1) {
            if (offset != -1) {
                return offset + "," + limit;
            } else if (page != -1) {
                return ((page - 1) * limit) + "," + limit;
            } else {
                return String.valueOf(limit);
            }
        } else { // when limit == -1, offset and page must be -1
            if (offset != -1 || page != -1) {
                throw new InvalidStatementException("Missing limit() when offset() or page() is specified.");
            } else {
                return null;
            }
        }
    }

    @IntRange(from = 0)
    public int count() {
        return conn.count(schema, getWhereClause(), getWhereArgs());
    }

    @NonNull
    public Observable<Integer> countAsObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(count());
                subscriber.onCompleted();
            }
        });
    }

    @Nullable
    public Model valueOrNull() {
        return conn.querySingle(schema, schema.getEscapedColumnNames(),
                getWhereClause(), getWhereArgs(), groupBy, having, orderBy);
    }

    @NonNull
    public Model value() throws NoValueException {
        Model model = valueOrNull();

        if (model == null) {
            throw new NoValueException("Expected single value but nothing for " + schema.getTableName());
        }

        return model;
    }

    @NonNull
    public Cursor query() {
        return conn.query(schema, schema.getEscapedColumnNames(), getWhereClause(),
                getWhereArgs(), groupBy, having, orderBy, getLimitClause());
    }

    @NonNull
    public List<Model> toList() {
        final ArrayList<Model> list = new ArrayList<>();

        forEach(new Action1<Model>() {
            @Override
            public void call(Model item) {
                list.add(item);
            }
        });

        return list;
    }

    public void forEach(@NonNull Action1<Model> action) {
        Cursor cursor = query();

        if (cursor.moveToFirst()) {
            do {
                action.call(schema.createModelFromCursor(conn, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @NonNull
    public Observable<Model> observable() {
        return Observable.create(new Observable.OnSubscribe<Model>() {
            @Override
            public void call(final Subscriber<? super Model> subscriber) {
                forEach(new Action1<Model>() {
                    @Override
                    public void call(Model item) {
                        subscriber.onNext(item);
                    }
                });

                subscriber.onCompleted();
            }
        });
    }
}
