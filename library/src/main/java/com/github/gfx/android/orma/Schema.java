package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

public interface Schema<T> {

    String getTableName();

    String[] getColumnNames();

    List<Column<?>> getColumns();

    ContentValues serializeToContentValues(@NonNull T todo);

    T newFromCursor(@NonNull Cursor cursor);

}
