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

import android.database.sqlite.SQLiteStatement;

class DefaultDatabaseStatement implements DatabaseStatement {

    private final SQLiteStatement statement;

    DefaultDatabaseStatement(SQLiteStatement statement) {
        this.statement = statement;
    }

    @Override
    public void bindAllArgsAsStrings(String[] bindArgs) {
        statement.bindAllArgsAsStrings(bindArgs);
    }

    @Override
    public int executeUpdateDelete() {
        return statement.executeUpdateDelete();
    }

    @Override
    public void close() {
        statement.close();
    }

    @Override
    public long executeInsert() {
        return statement.executeInsert();
    }

    @Override
    public void bindNull(int index) {
        statement.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        statement.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        statement.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        statement.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        statement.bindBlob(index, value);
    }
}
