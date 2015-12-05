package com.github.gfx.android.orma.internal;

import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OrmaConditionBase<T, C extends OrmaConditionBase<?, ?>> {

    protected final OrmaConnection conn;

    protected final Schema<T> schema;

    protected String whereConjunction = " AND ";

    @Nullable
    protected StringBuilder whereClause;

    @Nullable
    protected List<String> whereArgs;

    public OrmaConditionBase(OrmaConnection conn, Schema<T> schema) {
        this.conn = conn;
        this.schema = schema;
    }


    /**
     * Builds general `where` clause with arguments.
     * e.g. {@code where("title = ? OR title = ?", a, b)}
     *
     * @param conditions SQLite's WHERE conditions.
     * @param args       Arguments bound to the {@code conditions}.
     * @return The receiver itself.
     */
    @SuppressWarnings("unchecked")
    public C where(@NonNull String conditions, @NonNull Object... args) {
        if (whereClause == null) {
            whereClause = new StringBuilder(conditions.length() + 2);
            whereArgs = new ArrayList<>(args.length);
        } else {
            whereClause.append(whereConjunction);
        }

        whereClause.append('(');
        whereClause.append(conditions);
        whereClause.append(')');

        for (Object arg : args) {
            if (arg == null) {
                whereArgs.add(null);
            } else if (arg instanceof Boolean) {
                whereArgs.add((Boolean) arg ? "1" : "0");
            } else {
                whereArgs.add(arg.toString());
            }
        }
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C where(@NonNull String conditions, @NonNull Collection<?> args) {
        return where(conditions, args.toArray());
    }

    @SuppressWarnings("unchecked")
    protected <ColumnType> C in(boolean not, @NonNull String columnName, @NonNull Collection<ColumnType> values) {
        StringBuilder clause = new StringBuilder();

        clause.append(columnName);
        if (not) {
            clause.append(" NOT ");
        }
        clause.append(" IN (");
        for (int i = 0, size = values.size(); i < size; i++) {
            clause.append('?');

            if ((i + 1) != values.size()) {
                clause.append(", ");
            }
        }
        clause.append(')');

        return where(clause.toString(), values);
    }

    @SuppressWarnings("unchecked")
    public C and() {
        whereConjunction = " AND ";
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C or() {
        whereConjunction = " OR ";
        return (C) this;
    }

    @Nullable
    protected String getWhereClause() {
        return whereClause != null ? whereClause.toString() : null;
    }

    @Nullable
    protected String[] getWhereArgs() {
        if (whereArgs != null) {
            String[] array = new String[whereArgs.size()];
            return whereArgs.toArray(array);
        } else {
            return null;
        }
    }
}
