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
package com.github.gfx.android.orma.example.handwritten;

import com.github.gfx.android.orma.core.Database;
import com.github.gfx.android.orma.core.DatabaseProvider;

import android.content.Context;

public class HandWrittenOpenHelper {

    static final int VERSION = 4;

    private final Context context;

    private final String name;

    private final DatabaseProvider provider;

    private Database db;

    public HandWrittenOpenHelper(Context context, String name, DatabaseProvider provider) {
        this.context = context;
        this.name = name;
        this.provider = provider;
    }

    public Database getWritableDatabase() {
        if (db == null) {
            db = provider.provideOnDiskDatabase(context, name, 0);
            onCreate(db);
        }
        return db;
    }

    public Database getReadableDatabase() {
        return getWritableDatabase();
    }

    private void onCreate(Database db) {
        db.execSQL("CREATE TABLE todo ("
                + "id INTEGER PRIMARY KEY,"
                + "title TEXT NOT NULL,"
                + "content TEXT NULL,"
                + "done BOOLEAN NOT NULL,"
                + "createdTime INTEGER NOT NULL"
                + ")");
        db.execSQL("CREATE INDEX title_on_todo ON todo (title)");
        db.execSQL("CREATE INDEX createdTimeMillis_on_todo ON todo (createdTime)");
    }
}
