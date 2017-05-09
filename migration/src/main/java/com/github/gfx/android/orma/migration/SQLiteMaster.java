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
package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.core.Database;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * Interface to the SQLite metadata table, {@code sqlite_master}.
 */
public class SQLiteMaster implements MigrationSchema {

    public static String TAG = "SQLiteMaster";

    public String type;

    public String name;

    public String tableName;

    public String sql;

    public List<SQLiteMaster> indexes = new ArrayList<>();

    public SQLiteMaster() {
    }

    public SQLiteMaster(String type, String name, String tableName, String sql) {
        this.type = type;
        this.name = name;
        this.tableName = tableName;
        this.sql = sql;
    }

    @NonNull
    public static boolean checkIfTableNameExists(@NonNull Database db, @NonNull String tableName) {
        return db.queryNumEntries("sqlite_master", "tbl_name = ?", new String[]{tableName}) != 0;
    }

    @NonNull
    public static SQLiteMaster findByTableName(@NonNull Database db, @NonNull String tableName) {
        Cursor cursor = db.rawQuery("SELECT type,name,tbl_name,sql FROM sqlite_master where tbl_name = ?",
                new String[]{tableName});
        try {
            Map<String, SQLiteMaster> tables = loadTables(cursor);
            if (tables.isEmpty()) {
                throw new NoSuchElementException("No such table: " + tableName);
            }
            return tables.get(tableName);
        } finally {
            cursor.close();
        }
    }

    @NonNull
    public static Map<String, SQLiteMaster> loadTables(@NonNull Database db) {
        Cursor cursor = db.rawQuery("SELECT type,name,tbl_name,sql FROM sqlite_master", null);
        try {
            return loadTables(cursor);
        } finally {
            cursor.close();
        }
    }

    @NonNull
    public static Map<String, SQLiteMaster> loadTables(Cursor cursor) {
        Map<String, SQLiteMaster> tables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(0); // "table" or "index"
                String name = cursor.getString(1); // table or index name
                String tableName = cursor.getString(2);
                String sql = cursor.getString(3);

                SQLiteMaster meta = tables.get(tableName);
                if (meta == null) {
                    meta = new SQLiteMaster();
                    tables.put(tableName, meta);
                }

                switch (type) {
                    case "table":
                        meta.type = type;
                        meta.name = name;
                        meta.tableName = tableName;
                        meta.sql = sql;
                        break;
                    case "index":
                        // sql=null for sqlite_autoindex_${table}_${columnIndex}
                        if (sql != null) {
                            meta.indexes.add(new SQLiteMaster(type, name, tableName, sql));
                        }
                        break;
                    default:
                        Log.w(TAG, "unsupported type:" + type);
                }
            } while (cursor.moveToNext());
        }
        return tables;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(sql);
        s.append("; ");

        for (SQLiteMaster index : indexes) {
            s.append(index);
            s.append("; ");
        }
        s.setLength(s.length() - "; ".length());
        return s.toString();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getCreateTableStatement() {
        return sql;
    }

    @Override
    public List<String> getCreateIndexStatements() {
        List<String> createIndexStatements = new ArrayList<>();
        for (SQLiteMaster index : indexes) {
            createIndexStatements.add(index.sql);
        }
        return createIndexStatements;
    }
}
