package com.github.gfx.android.orma;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

/**
 * Represents a prepared statement to insert models in batch.
 */
public class Inserter<T> {

    final OrmaConnection conn;

    final Schema<T> schema;

    final SQLiteStatement statement;

    public Inserter(OrmaConnection conn, Schema<T> schema, SQLiteStatement statement) {
        this.conn = conn;
        this.schema = schema;
        this.statement = statement;
    }

    /**
     * <p>Inserts {@param model} into a table.</p>
     * <p>Note that {@code Inserter<T>} does not provide `observable()` method because prepared statements should always be
     * grouped by transaction. </p>
     *
     * @return The last inserted row id.
     */
    public long execute(@NonNull T model) {
        schema.bindArgs(conn, statement, model);
        return statement.executeInsert();
    }
}
