package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.OrmaCore;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.example.Book;

public class Todo_Relation extends Relation<Book, Todo_Relation> {

    public Todo_Relation(OrmaCore orma, Todo_Schema schema) {
        super(orma, schema);
    }

}
