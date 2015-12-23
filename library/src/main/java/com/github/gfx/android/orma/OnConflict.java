package com.github.gfx.android.orma;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        OnConflict.NONE,
        OnConflict.ABORT,
        OnConflict.FAIL,
        OnConflict.IGNORE,
        OnConflict.REPLACE,
        OnConflict.ROLLBACK,
})
@Retention(RetentionPolicy.SOURCE)
public @interface OnConflict {

    int NONE = SQLiteDatabase.CONFLICT_NONE;
    int ABORT = SQLiteDatabase.CONFLICT_ABORT;
    int FAIL = SQLiteDatabase.CONFLICT_FAIL;
    int IGNORE = SQLiteDatabase.CONFLICT_IGNORE;
    int REPLACE = SQLiteDatabase.CONFLICT_REPLACE;
    int ROLLBACK = SQLiteDatabase.CONFLICT_ROLLBACK;
}
