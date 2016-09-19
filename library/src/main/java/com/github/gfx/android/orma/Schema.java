/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma;

import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.annotation.PrimaryKey;
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

    /**
     * @return A general escaped table name, used in {@code UPDATE} and {@code DELETE}.
     */
    @NonNull
    String getEscapedTableName();

    @NonNull
    String getTableAlias();

    @NonNull
    String getEscapedTableAlias();

    /**
     * @return An escaped table name, which may includes {@code JOIN} clauses, used in {@code SELECT}.
     */
    @NonNull
    String getSelectFromTableClause();

    @NonNull
    ColumnDef<Model, ?> getPrimaryKey();

    /**
     * @return Escaped column names for {@code SELECT}, which may includes joined table's columns.
     */
    @NonNull
    String[] getDefaultResultColumns();

    @NonNull
    List<ColumnDef<Model, ?>> getColumns();

    @NonNull
    String getCreateTableStatement();

    @NonNull
    String getDropTableStatement();

    @NonNull
    List<String> getCreateIndexStatements();

    @NonNull
    String getInsertStatement(@OnConflict int onConflictAlgorithm, boolean withoutAutoId);

    Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Model mode, boolean withoutAutoId);

    /**
     * @param conn          Used to retrieve instances that depends on a connection
     * @param statement     What to bind columns
     * @param model         The target model
     * @param withoutAutoId If {@code true}, the primary key with {@link PrimaryKey#auto()} is omitted in the {@code INSERT}
     *                      statement.
     */
    void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement, @NonNull Model model,
            boolean withoutAutoId);

    @NonNull
    Model newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int offset);
}
