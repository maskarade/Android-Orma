package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

public abstract class Schema<T> {

    public abstract String getTableName();

    public abstract String[] getColumnNames();

    public abstract List<Column<?>> getColumns();

    public abstract ContentValues serializeToContentValues(@NonNull T todo);

    public abstract T newFromCursor(@NonNull Cursor cursor);

}
