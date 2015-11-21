package com.github.gfx.android.orma;

import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.content.ContentValues;
import android.support.annotation.NonNull;

public class Updater<T, C extends Updater> extends OrmaConditionBase<T, C> {

    final protected ContentValues contents = new ContentValues();

    public Updater(OrmaConnection conn, Schema<T> schema) {
        super(conn, schema);
    }

    @NonNull
    public ContentValues getContentValues() {
        return contents;
    }

    public int execute() {
        return conn.update(schema.getTableName(), contents, getWhereClause(), getWhereArgs());
    }
}
