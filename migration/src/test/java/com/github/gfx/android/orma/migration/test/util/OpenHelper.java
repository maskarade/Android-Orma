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

package com.github.gfx.android.orma.migration.test.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class OpenHelper extends SQLiteOpenHelper {

    final List<SchemaData> schemas;

    final List<String> fixtures;

    public OpenHelper(Context context, List<SchemaData> schemas, List<String> fixtures) {
        super(context, null, null, 1);
        this.schemas = schemas;
        this.fixtures = fixtures;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (SchemaData ddl : schemas) {
            db.execSQL(ddl.getCreateTableStatement());
            for (String sql : ddl.getCreateIndexStatements()) {
                db.execSQL(sql);
            }
        }
        for (String sql : fixtures) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
