package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

public interface Schema<T> {

    String getTableName();

    String[] getColumnNames();

    List<ColumnDef<?>> getColumns();

    ContentValues serializeModelToContentValues(@NonNull T todo);

    T createModelFromCursor(@NonNull Cursor cursor);

}
