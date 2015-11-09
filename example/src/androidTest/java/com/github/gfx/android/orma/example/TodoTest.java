package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.example.orma.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TodoTest {

    OrmaDatabase db;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getOrma().resetDatabase();

        {
            Todo todo = new Todo();
            todo.title = "today";
            todo.content = "milk, banana";
            db.insert(todo);
        }

        {
            Todo todo = new Todo();
            todo.title = "friday";
            todo.content = "apple";
            db.insert(todo);
        }
    }

    @Test
    public void count() throws Exception {
        assertThat(db.fromTodo().count(), is(2L));
    }

    @Test
    public void toList() throws Exception {
        List<Todo> todos = db.fromTodo().toList();
        assertThat(todos, hasSize(2));
        assertThat(todos.get(0).title, is("today"));
        assertThat(todos.get(0).content, is("milk, banana"));

        assertThat(todos.get(1).title, is("friday"));
        assertThat(todos.get(1).content, is("apple"));
    }

    @Test
    public void single() throws Exception {
        Todo todo = db.fromTodo().single();

        assert todo != null;
        assertThat(todo.title, is("today"));
        assertThat(todo.content, is("milk, banana"));
    }

    @Test
    public void whereEquals() throws Exception {
        List<Todo> todos = db.fromTodo().where("title = ?", "today").toList();
        assertThat(todos, hasSize(1));
        assertThat(todos.get(0).title, is("today"));
        assertThat(todos.get(0).content, is("milk, banana"));
    }

    @Test
    public void whereLike() throws Exception {
        List<Todo> todos = db.fromTodo().where("title LIKE ?", "t%").toList();
        assertThat(todos, hasSize(1));
        assertThat(todos.get(0).title, is("today"));
        assertThat(todos.get(0).content, is("milk, banana"));
    }

    @Test
    public void orderBy() throws Exception {
        List<Todo> todos = db.fromTodo().orderBy("id DESC").toList();
        assertThat(todos, hasSize(2));
        assertThat(todos.get(1).title, is("today"));
        assertThat(todos.get(1).content, is("milk, banana"));

        assertThat(todos.get(0).title, is("friday"));
        assertThat(todos.get(0).content, is("apple"));
    }
}
