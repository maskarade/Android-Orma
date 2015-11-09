package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.Orma;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.example.Todo;

public class Todo_Relation extends Relation<Todo, Todo_Relation> {

    public Todo_Relation(Orma orma, Todo_Schema schema) {
        super(orma, schema);
    }

}
