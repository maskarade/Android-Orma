package com.github.gfx.android.orma;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Relation<T, R extends Relation> {

    protected final OrmaCore orma;

    protected final Schema<T> schema;

    @Nullable
    protected StringBuilder whereClause;

    @Nullable
    protected List<String> whereArgs;

    @Nullable
    protected String groupBy;

    @Nullable
    protected String having;

    @Nullable
    protected String orderBy;

    protected long limit = -1;

    protected long offset = -1;

    public Relation(OrmaCore orma, Schema<T> schema) {
        this.orma = orma;
        this.schema = schema;
    }

    @SuppressWarnings("unchecked")
    public R where(@NonNull String clause, @NonNull Object... args) {
        if (whereClause == null) {
            whereClause = new StringBuilder(clause.length() + 2);
            whereArgs = new ArrayList<>(args.length);
        } else {
            whereClause.append(" AND ");
        }

        whereClause.append('(');
        whereClause.append(clause);
        whereClause.append(')');

        for (Object arg : args) {
            if (arg == null) {
                whereArgs.add(null);
            } else {
                whereArgs.add(arg.toString());
            }
        }

        return (R) this;
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
        return orma.count(schema.getTableName(), getWhereClause(), getWhereArgs());
    }

    @Nullable
    private String getWhereClause() {
        return whereClause != null ? whereClause.toString() : null;
    }

    @Nullable
    private String[] getWhereArgs() {
        if (whereArgs != null) {
            String[] array = new String[whereArgs.size()];
            return whereArgs.toArray(array);
        } else {
            return null;
        }
    }

    public long update(ModelBuilder<T> modelBuilder) {
        assertNoExtraClausesForDeleteOrUpdate("update");
        return orma.update(schema.getTableName(), modelBuilder.buildContentValues(), getWhereClause(), getWhereArgs());
    }

    public int delete() {
        assertNoExtraClausesForDeleteOrUpdate("delete");
        return orma.delete(schema.getTableName(), getWhereClause(), getWhereArgs());
    }

    void assertNoExtraClausesForDeleteOrUpdate(String statementName) {
        List<String> extraClauses = null;
        if (groupBy != null) {
            extraClauses = new ArrayList<>();
            extraClauses.add("groupBy");
        }
        if (having != null) {
            if (extraClauses == null) {
                extraClauses = new ArrayList<>();
            }
            extraClauses.add("having");
        }
        if (orderBy != null) {
            if (extraClauses == null) {
                extraClauses = new ArrayList<>();
            }
            extraClauses.add("orderBy");
        }
        if (limit != -1) {
            if (extraClauses == null) {
                extraClauses = new ArrayList<>();
            }
            extraClauses.add("limit");
        }
        if (offset != -1) {
            if (extraClauses == null) {
                extraClauses = new ArrayList<>();
            }
            extraClauses.add("offset");
        }
        if (extraClauses != null) {
            throw new InvalidStatementException(
                    "Extra clauses for " + statementName + ": " + TextUtils.join(", ", extraClauses));
        }
    }

    @Nullable
    public T single() {
        Cursor cursor = orma.query(schema.getTableName(), schema.getColumnNames(), getWhereClause(),
                getWhereArgs(), groupBy, having, orderBy, getLimitClause());

        T model = null;

        if (cursor.moveToFirst()) {
            model = schema.newFromCursor(cursor);
        }
        cursor.close();

        return model;
    }

    @NonNull
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Cursor cursor = orma.query(schema.getTableName(), schema.getColumnNames(), getWhereClause(),
                getWhereArgs(), groupBy, having, orderBy, getLimitClause());

        if (cursor.moveToFirst()) {
            do {
                list.add(schema.newFromCursor(cursor));
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    public static class InvalidStatementException extends RuntimeException {

        public InvalidStatementException(String detailMessage) {
            super(detailMessage);
        }
    }
}
