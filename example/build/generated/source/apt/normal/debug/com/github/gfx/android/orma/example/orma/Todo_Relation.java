package com.github.gfx.android.orma.example.orma;

import android.content.ContentValues;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.annotation.OnConflict;
import java.util.Arrays;
import java.util.Collection;

public class Todo_Relation extends Relation<Todo, Todo_Relation> {
  final Todo_Schema schema;

  public Todo_Relation(OrmaConnection conn, Todo_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Todo_Relation(Todo_Relation that) {
    super(that);
    this.schema = that.getSchema();
  }

  @Override
  public Todo_Relation clone() {
    return new Todo_Relation(this);
  }

  @NonNull
  @Override
  public Todo_Schema getSchema() {
    return schema;
  }

  @NonNull
  @CheckResult
  public Todo reload(@NonNull Todo model) {
    return selector().idEq(model.id).value();
  }

  @NonNull
  @Override
  public Todo upsertWithoutTransaction(@NonNull Todo model) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("`title`", model.title);
    contentValues.put("`content`", model.content != null ? model.content : null);
    contentValues.put("`done`", model.done);
    contentValues.put("`createdTime`", BuiltInSerializers.serializeDate(model.createdTime));
    if (model.id != 0) {
      int updatedRows = updater().idEq(model.id).putAll(contentValues).execute();
      if (updatedRows != 0) {
        return selector().idEq(model.id).value();
      }
    }
    long rowId = conn.insert(schema, contentValues, OnConflict.NONE);
    return conn.findByRowId(schema, rowId);
  }

  @NonNull
  @Override
  public Todo_Selector selector() {
    return new Todo_Selector(this);
  }

  @NonNull
  @Override
  public Todo_Updater updater() {
    return new Todo_Updater(this);
  }

  @NonNull
  @Override
  public Todo_Deleter deleter() {
    return new Todo_Deleter(this);
  }

  public Todo_Relation titleEq(@NonNull String title) {
    return where(schema.title, "=", title);
  }

  public Todo_Relation titleNotEq(@NonNull String title) {
    return where(schema.title, "<>", title);
  }

  public Todo_Relation titleIn(@NonNull Collection<String> values) {
    return in(false, schema.title, values);
  }

  public Todo_Relation titleNotIn(@NonNull Collection<String> values) {
    return in(true, schema.title, values);
  }

  public final Todo_Relation titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_Relation titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Relation titleGlob(@NonNull String pattern) {
    return where(schema.title, "GLOB", pattern);
  }

  public Todo_Relation titleNotGlob(@NonNull String pattern) {
    return where(schema.title, "NOT GLOB", pattern);
  }

  public Todo_Relation titleLike(@NonNull String pattern) {
    return where(schema.title, "LIKE", pattern);
  }

  public Todo_Relation titleNotLike(@NonNull String pattern) {
    return where(schema.title, "NOT LIKE", pattern);
  }

  public Todo_Relation titleLt(@NonNull String title) {
    return where(schema.title, "<", title);
  }

  public Todo_Relation titleLe(@NonNull String title) {
    return where(schema.title, "<=", title);
  }

  public Todo_Relation titleGt(@NonNull String title) {
    return where(schema.title, ">", title);
  }

  public Todo_Relation titleGe(@NonNull String title) {
    return where(schema.title, ">=", title);
  }

  public Todo_Relation doneEq(boolean done) {
    return where(schema.done, "=", done);
  }

  public Todo_Relation doneNotEq(boolean done) {
    return where(schema.done, "<>", done);
  }

  public Todo_Relation doneIn(@NonNull Collection<Boolean> values) {
    return in(false, schema.done, values);
  }

  public Todo_Relation doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, schema.done, values);
  }

  public final Todo_Relation doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_Relation doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Relation doneLt(boolean done) {
    return where(schema.done, "<", done);
  }

  public Todo_Relation doneLe(boolean done) {
    return where(schema.done, "<=", done);
  }

  public Todo_Relation doneGt(boolean done) {
    return where(schema.done, ">", done);
  }

  public Todo_Relation doneGe(boolean done) {
    return where(schema.done, ">=", done);
  }

  public Todo_Relation idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Todo_Relation idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Todo_Relation idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Todo_Relation idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Todo_Relation idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_Relation idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Relation idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Todo_Relation idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Todo_Relation idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Todo_Relation idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Todo_Relation idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  public Todo_Relation orderByTitleAsc() {
    return orderBy(schema.title.orderInAscending());
  }

  public Todo_Relation orderByTitleDesc() {
    return orderBy(schema.title.orderInDescending());
  }

  public Todo_Relation orderByDoneAsc() {
    return orderBy(schema.done.orderInAscending());
  }

  public Todo_Relation orderByDoneDesc() {
    return orderBy(schema.done.orderInDescending());
  }

  public Todo_Relation orderByCreatedTimeAsc() {
    return orderBy(schema.createdTime.orderInAscending());
  }

  public Todo_Relation orderByCreatedTimeDesc() {
    return orderBy(schema.createdTime.orderInDescending());
  }
}
