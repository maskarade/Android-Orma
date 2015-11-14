package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import java.util.List;

public interface Schema<T> {

    String getTableName();

    String[] getColumnNames();

    List<ColumnDef<?>> getColumns();

    ContentValues serializeModelToContentValues(@NonNull T model);

    T createModelFromCursor(@NonNull Cursor cursor);

    void bindArgs(@NonNull SQLiteStatement statement, @NonNull T model);
}
