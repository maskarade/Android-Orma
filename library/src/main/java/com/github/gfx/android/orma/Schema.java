package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public interface Schema<T> {

    @NonNull
    String getTableName();

    @Nullable
    ColumnDef<?> getPrimaryKey();

    @NonNull
    String[] getColumnNames();

    @NonNull
    List<ColumnDef<?>> getColumns();

    @NonNull
    ContentValues serializeModelToContentValues(@NonNull T model);

    void populateValuesIntoModel(@NonNull OrmaConnection conn, @NonNull Cursor cursor, @NonNull T model);

    void bindArgs(@NonNull SQLiteStatement statement, @NonNull T model);

    @NonNull
    T createModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor);
}
