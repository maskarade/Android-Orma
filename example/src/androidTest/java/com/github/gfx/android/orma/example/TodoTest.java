package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.example.orma.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TodoTest {

    OrmaDatabase db;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getOrma().resetDatabase();
    }

    @Test
    public void testInsertAndCount() throws Exception {
        Todo todo = new Todo();
        todo.title = "hoge";
        todo.content = "fuga";
        db.insert(todo);

        assertThat(db.fromTodo().count(), is(1L));

    }
}
