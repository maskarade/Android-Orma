package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.OrmaCore;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.Book;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class OrmaDatabase {

    static final Todo_Schema todoSchema = new Todo_Schema();

    static final List<Schema<?>> schemas = Arrays.<Schema<?>>asList(todoSchema);

    final OrmaCore orma;

    public OrmaDatabase(@NonNull OrmaCore orma) {
        this.orma = orma;
    }

    public OrmaDatabase(@NonNull Context context, String filename) {
        this(new OrmaCore(context, filename, schemas));
    }

    public OrmaCore getOrma() {
        return orma;
    }

    public void transaction(@NonNull TransactionTask task) {
        orma.transaction(task);
    }


    public Todo_Relation fromTodo() {
        return new Todo_Relation(orma, todoSchema);
    }

    public long insert(Book model) {
        // FIXME
        return orma.insert(todoSchema.getTableName(), todoSchema.serializeModelToContentValues(model));
    }

    public long update(Book model, String whereClause, String[] whereArgs) {
        // FIXME
        return orma.update(todoSchema.getTableName(), todoSchema.serializeModelToContentValues(model), whereClause, whereArgs);
    }
}
