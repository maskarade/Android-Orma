package com.github.gfx.android.orma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ConditionBase<T, C extends ConditionBase> {

    protected final OrmaConnection connection;

    protected final Schema<T> schema;

    public ConditionBase(OrmaConnection connection, Schema<T> schema) {
        this.connection = connection;
        this.schema = schema;
    }

    @Nullable
    protected StringBuilder whereClause;

    @Nullable
    protected List<String> whereArgs;

    @SuppressWarnings("unchecked")
    public C where(@NonNull String clause, @NonNull Object... args) {
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
