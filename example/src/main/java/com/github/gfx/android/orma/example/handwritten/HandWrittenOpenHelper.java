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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HandWrittenOpenHelper extends SQLiteOpenHelper {

    int VERSION = 4;

    public HandWrittenOpenHelper(Context context, String name) {
        super(context, name, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE todo ("
                + "id INTEGER PRIMARY KEY,"
                + "title TEXT NOT NULL,"
                + "content TEXT NULL,"
                + "done BOOLEAN NOT NULL,"
                + "createdTimeMillis INTEGER NOT NULL"
                + ")");
        db.execSQL("CREATE INDEX title_on_todo ON todo (title)");
        db.execSQL("CREATE INDEX createdTimeMillis_on_todo ON todo (createdTimeMillis)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE todo");
    }
}
