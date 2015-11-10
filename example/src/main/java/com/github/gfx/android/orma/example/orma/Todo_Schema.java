package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.Column;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.example.Todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class Todo_Schema implements Schema<Todo> {

    public static String $TABLE_NAME = "Todo";

    public static Column<Integer> id = new Column<>("id", int.class, false, true, false, false);

    public static Column<String> title = new Column<>("title", String.class, false, false, false, false);

    public static Column<String> content = new Column<>("content", String.class, false, false, false, false);

    public static String[] $COLUMN_NAMES = {
            id.name,
            title.name,
            content.name
    };

    public static List<Column<?>> $COLUMNS = Arrays.<Column<?>>asList(
            id,
            title,
            content
    );


    @Override
    public String getTableName() {
        return $TABLE_NAME;
    }

    @Override
    public String[] getColumnNames() {
        return $COLUMN_NAMES;
    }

    @Override
    public List<Column<?>> getColumns() {
        return $COLUMNS;
    }

    @Override
    public ContentValues serializeToContentValues(@NonNull Todo todo) {
        ContentValues contentValues = new ContentValues($COLUMN_NAMES.length);
        if (todo.id != 0) {
            contentValues.put(id.name, todo.id);
        }
        contentValues.put(title.name, todo.title);
        contentValues.put(content.name, todo.content);
        return contentValues;
    }

    @Override
    public Todo newFromCursor(@NonNull Cursor cursor) {
        Todo todo = new Todo();
        todo.id = cursor.getInt(0);
        todo.title = cursor.getString(1);
        todo.content = cursor.getString(2);
        return todo;
    }
}
