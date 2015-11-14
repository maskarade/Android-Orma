package com.github.gfx.android.orma;

import android.database.sqlite.SQLiteStatement;

public class Inserter<T> {

    final Schema<T> schema;

    final SQLiteStatement statement;

    public Inserter(Schema<T> schema, SQLiteStatement statement) {
        this.schema = schema;
        this.statement = statement;
    }

    public long insert(T model) {
        statement.clearBindings();
        schema.bindArgs(statement, model);
        return statement.executeInsert();
    }
}
