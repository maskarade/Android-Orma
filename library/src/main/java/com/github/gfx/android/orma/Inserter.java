package com.github.gfx.android.orma;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

public class Inserter<T> {

    final OrmaConnection conn;

    final Schema<T> schema;

    final SQLiteStatement statement;

    public Inserter(OrmaConnection conn, Schema<T> schema, SQLiteStatement statement) {
        this.conn = conn;
        this.schema = schema;
        this.statement = statement;
    }

    public long execute(@NonNull T model) {
        schema.bindArgs(conn, statement, model);
        return statement.executeInsert();
    }
}
