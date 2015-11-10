package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.example.Todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class Todo_Schema implements Schema<Todo> {

    public static String $TABLE_NAME = "Todo";

    public static ColumnDef<Integer> id = new ColumnDef<>("id", int.class, false, true, false, false);

    public static ColumnDef<String> title = new ColumnDef<>("title", String.class, false, false, false, false);

    public static ColumnDef<String> content = new ColumnDef<>("content", String.class, false, false, false, false);

    public static String[] $COLUMN_NAMES = {
            id.name,
            title.name,
            content.name
    };

    public static List<ColumnDef<?>> $COLUMNS = Arrays.<ColumnDef<?>>asList(
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
    public List<ColumnDef<?>> getColumns() {
        return $COLUMNS;
    }

    @Override
    public ContentValues serializeModelToContentValues(@NonNull Todo todo) {
        ContentValues contentValues = new ContentValues($COLUMN_NAMES.length);
        if (todo.id != 0) {
            contentValues.put(id.name, todo.id);
        }
        contentValues.put(title.name, todo.title);
        contentValues.put(content.name, todo.content);
        return contentValues;
    }

    @Override
    public Todo createModelFromCursor(@NonNull Cursor cursor) {
        Todo todo = new Todo();
        todo.id = cursor.getInt(0);
        todo.title = cursor.getString(1);
        todo.content = cursor.getString(2);
        return todo;
    }
}
