package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.rx.RxOrmaConnection;
import com.github.gfx.android.orma.rx.RxUpdater;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class Todo_Updater extends RxUpdater<Todo, Todo_Updater> {
  final Todo_Schema schema;

  public Todo_Updater(RxOrmaConnection conn, Todo_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Todo_Updater(Todo_Updater that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Todo_Updater(Todo_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Todo_Updater clone() {
    return new Todo_Updater(this);
  }

  @NonNull
  @Override
  public Todo_Schema getSchema() {
    return schema;
  }

  public Todo_Updater title(@NonNull String title) {
    contents.put("`title`", title);
    return this;
  }

  public Todo_Updater content(@Nullable String content) {
    if (content == null) {
      contents.putNull("`content`");
    }
    else {
      contents.put("`content`", content);
    }
    return this;
  }

  public Todo_Updater done(boolean done) {
    contents.put("`done`", done);
    return this;
  }

  public Todo_Updater createdTime(@NonNull Date createdTime) {
    contents.put("`createdTime`", BuiltInSerializers.serializeDate(createdTime));
    return this;
  }

  public Todo_Updater titleEq(@NonNull String title) {
    return where(schema.title, "=", title);
  }

  public Todo_Updater titleNotEq(@NonNull String title) {
    return where(schema.title, "<>", title);
  }

  public Todo_Updater titleIn(@NonNull Collection<String> values) {
    return in(false, schema.title, values);
  }

  public Todo_Updater titleNotIn(@NonNull Collection<String> values) {
    return in(true, schema.title, values);
  }

  public final Todo_Updater titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_Updater titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Updater titleGlob(@NonNull String pattern) {
    return where(schema.title, "GLOB", pattern);
  }

  public Todo_Updater titleNotGlob(@NonNull String pattern) {
    return where(schema.title, "NOT GLOB", pattern);
  }

  public Todo_Updater titleLike(@NonNull String pattern) {
    return where(schema.title, "LIKE", pattern);
  }

  public Todo_Updater titleNotLike(@NonNull String pattern) {
    return where(schema.title, "NOT LIKE", pattern);
  }

  public Todo_Updater titleLt(@NonNull String title) {
    return where(schema.title, "<", title);
  }

  public Todo_Updater titleLe(@NonNull String title) {
    return where(schema.title, "<=", title);
  }

  public Todo_Updater titleGt(@NonNull String title) {
    return where(schema.title, ">", title);
  }

  public Todo_Updater titleGe(@NonNull String title) {
    return where(schema.title, ">=", title);
  }

  public Todo_Updater doneEq(boolean done) {
    return where(schema.done, "=", done);
  }

  public Todo_Updater doneNotEq(boolean done) {
    return where(schema.done, "<>", done);
  }

  public Todo_Updater doneIn(@NonNull Collection<Boolean> values) {
    return in(false, schema.done, values);
  }

  public Todo_Updater doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, schema.done, values);
  }

  public final Todo_Updater doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_Updater doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Updater doneLt(boolean done) {
    return where(schema.done, "<", done);
  }

  public Todo_Updater doneLe(boolean done) {
    return where(schema.done, "<=", done);
  }

  public Todo_Updater doneGt(boolean done) {
    return where(schema.done, ">", done);
  }

  public Todo_Updater doneGe(boolean done) {
    return where(schema.done, ">=", done);
  }

  public Todo_Updater idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Todo_Updater idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Todo_Updater idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Todo_Updater idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Todo_Updater idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_Updater idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Updater idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Todo_Updater idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Todo_Updater idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Todo_Updater idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Todo_Updater idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }
}
