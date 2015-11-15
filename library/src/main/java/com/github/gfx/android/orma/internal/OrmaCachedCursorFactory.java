package com.github.gfx.android.orma.internal;


import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class OrmaCachedCursorFactory implements SQLiteDatabase.CursorFactory {

    SQLiteCursor cursor;

    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        if (cursor == null) {
            cursor = new SQLiteCursor(driver, editTable, query);
        }
        return cursor;
    }

}
