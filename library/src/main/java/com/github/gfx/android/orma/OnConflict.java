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
