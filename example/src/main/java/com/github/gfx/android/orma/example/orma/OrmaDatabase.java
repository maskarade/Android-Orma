package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.Todo;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class OrmaDatabase {

    static final Todo_Schema todoSchema = new Todo_Schema();

    static final List<Schema<?>> schemas = Arrays.<Schema<?>>asList(todoSchema);

    final OrmaConnection orma;

    public OrmaDatabase(@NonNull OrmaConnection orma) {
        this.orma = orma;
    }

    public OrmaDatabase(@NonNull Context context, String filename) {
        this(new OrmaConnection(context, filename, schemas));
    }

    public OrmaConnection getOrma() {
        return orma;
    }

    public void transaction(@NonNull TransactionTask task) {
        orma.transaction(task);
    }


    public Todo_Relation fromTodo() {
        return new Todo_Relation(orma, todoSchema);
    }

    public long insert(Todo model) {
        return orma.insert(todoSchema, model);
    }
}
