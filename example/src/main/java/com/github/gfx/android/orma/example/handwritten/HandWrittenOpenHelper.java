package com.github.gfx.android.orma.example.handwritten;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HandWrittenOpenHelper extends SQLiteOpenHelper {

    public HandWrittenOpenHelper(Context context, String name) {
        super(context, name, null, 3);
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
