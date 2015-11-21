package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

import java.lang.reflect.Type;

public class ColumnDef<T /* type param is not used */> {

    public final String name;

    public final Type type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean autoincrement;

    public final boolean autoId;

    public final boolean indexed;

    public final boolean unique;

    public ColumnDef(String name, Type type, boolean nullable, boolean primaryKey, boolean autoincrement, boolean autoId,
            boolean indexed, boolean unique) {
        this.name = name;
        this.type = type;
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
