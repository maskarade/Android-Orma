package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

public class ColumnDef<T> {

    public final String name;

    public final Class<T> type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean autoincrement;

    public final boolean autoId;

    public final boolean indexed;

    public final boolean unique;

    public ColumnDef(String name, Class<?> type, boolean nullable, boolean primaryKey, boolean autoincrement, boolean autoId,
            boolean indexed, boolean unique) {
        this.name = name;
        this.type = (Class<T>) type;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.autoincrement = autoincrement;
        this.autoId = autoId;
        this.indexed = indexed;
        this.unique = unique;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
