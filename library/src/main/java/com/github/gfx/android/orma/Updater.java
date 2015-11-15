package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.support.annotation.NonNull;

public class Updater<T, C extends Updater> extends ConditionBase<T, C> {

    final protected ContentValues contents = new ContentValues();

    public Updater(OrmaConnection connection, Schema<T> schema) {
        super(connection, schema);
    }

    @NonNull
    public ContentValues getContentValues() {
        return contents;
    }

    public int execute() {
        return connection.update(schema.getTableName(), contents, getWhereClause(), getWhereArgs());
    }
}
