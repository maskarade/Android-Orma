package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import java.util.List;

public interface Schema<T> {

    @NonNull
    String getTableName();

    @NonNull
    String[] getColumnNames();

    @NonNull
    List<ColumnDef<?>> getColumns();

    @NonNull
    ContentValues serializeModelToContentValues(@NonNull T model);

    void populateValuesIntoModel(@NonNull Cursor cursor, @NonNull T model);

    void bindArgs(@NonNull SQLiteStatement statement, @NonNull T model);

    @NonNull
    T createModelFromCursor(@NonNull Cursor cursor);
}
