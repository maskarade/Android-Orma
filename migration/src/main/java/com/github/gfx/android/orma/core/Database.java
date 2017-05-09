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

package com.github.gfx.android.orma.core;

import android.content.ContentValues;
import android.database.Cursor;

public interface Database {

    long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm);

    int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    Cursor rawQuery(String sql, String[] selectionArgs);

    DatabaseStatement compileStatement(String sql);

    void beginTransaction();

    void beginTransactionNonExclusive();

    void setTransactionSuccessful();

    void endTransaction();

    boolean inTransaction();

    void execSQL(String sql);

    void execSQL(String sql, Object[] bindArgs);

    void setForeignKeyConstraintsEnabled(boolean enable);

    boolean enableWriteAheadLogging();

    boolean isWriteAheadLoggingEnabled();

    long longForQuery(String query, String[] selectionArgs);

    int getVersion();

    void setVersion(int version);

    long insertOrThrow(String table, String nullColumnHack, ContentValues values);

    Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy, String limit);

    long queryNumEntries(String table, String selection, String[] selectionArgs);

    void close();
}
