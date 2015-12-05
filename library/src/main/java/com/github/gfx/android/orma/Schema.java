package com.github.gfx.android.orma;

import com.github.gfx.android.orma.migration.MigrationSchema;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import java.util.List;

public interface Schema<Model> extends MigrationSchema {

    @NonNull
    Class<Model> getModelClass();

    @NonNull
    String getTableName();

    @NonNull
    ColumnDef<?> getPrimaryKey();

    @NonNull
    String[] getEscapedColumnNames();

    @NonNull
    List<ColumnDef<?>> getColumns();

    @NonNull
    String getCreateTableStatement();

    @NonNull
    String getDropTableStatement();

    @NonNull
    List<String> getCreateIndexStatements();

    @NonNull
    String getInsertStatement();

    void populateValuesIntoModel(@NonNull OrmaConnection conn, @NonNull Cursor cursor, @NonNull Model model);

    void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement, @NonNull Model model);

    @NonNull
    Model createModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor);
}
