package com.github.gfx.android.orma.example.orma;

import android.content.ContentValues;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

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

  @Deprecated
  public Todo_Relation contentIsNull() {
    return where(schema.content, " IS NULL");
  }

  @Deprecated
  public Todo_Relation contentIsNotNull() {
    return where(schema.content, " IS NOT NULL");
  }

  @Deprecated
  public Todo_Relation contentEq(@NonNull String content) {
    return where(schema.content, "=", content);
  }

  @Deprecated
  public Todo_Relation contentNotEq(@NonNull String content) {
    return where(schema.content, "<>", content);
  }

  @Deprecated
  public Todo_Relation contentIn(@NonNull Collection<String> values) {
    return in(false, schema.content, values);
  }

  @Deprecated
  public Todo_Relation contentNotIn(@NonNull Collection<String> values) {
    return in(true, schema.content, values);
  }

  @Deprecated
  public final Todo_Relation contentIn(@NonNull String... values) {
    return contentIn(Arrays.asList(values));
  }

  @Deprecated
  public final Todo_Relation contentNotIn(@NonNull String... values) {
    return contentNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Todo_Relation contentLt(@NonNull String content) {
    return where(schema.content, "<", content);
  }

  @Deprecated
  public Todo_Relation contentLe(@NonNull String content) {
    return where(schema.content, "<=", content);
  }

  @Deprecated
  public Todo_Relation contentGt(@NonNull String content) {
    return where(schema.content, ">", content);
  }

  @Deprecated
  public Todo_Relation contentGe(@NonNull String content) {
    return where(schema.content, ">=", content);
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

  @Deprecated
  public Todo_Relation createdTimeEq(@NonNull Date createdTime) {
    return where(schema.createdTime, "=", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Relation createdTimeNotEq(@NonNull Date createdTime) {
    return where(schema.createdTime, "<>", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Relation createdTimeIn(@NonNull Collection<Date> values) {
    return in(false, schema.createdTime, values, new Function1<Date, Long>() {
      @Override
      public Long apply(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  @Deprecated
  public Todo_Relation createdTimeNotIn(@NonNull Collection<Date> values) {
    return in(true, schema.createdTime, values, new Function1<Date, Long>() {
      @Override
      public Long apply(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  @Deprecated
  public final Todo_Relation createdTimeIn(@NonNull Date... values) {
    return createdTimeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Todo_Relation createdTimeNotIn(@NonNull Date... values) {
    return createdTimeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Todo_Relation createdTimeLt(@NonNull Date createdTime) {
    return where(schema.createdTime, "<", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Relation createdTimeLe(@NonNull Date createdTime) {
    return where(schema.createdTime, "<=", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Relation createdTimeGt(@NonNull Date createdTime) {
    return where(schema.createdTime, ">", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Relation createdTimeGe(@NonNull Date createdTime) {
    return where(schema.createdTime, ">=", BuiltInSerializers.serializeDate(createdTime));
  }

  /**
   * To build a condition <code>createdTime BETWEEN a AND b</code>, which is equivalent to <code>a <= createdTime AND createdTime <= b</code>.
   */
  @Deprecated
  public Todo_Relation createdTimeBetween(@NonNull Date createdTimeA, @NonNull Date createdTimeB) {
    return whereBetween(schema.createdTime, BuiltInSerializers.serializeDate(createdTimeA), BuiltInSerializers.serializeDate(createdTimeB));
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

  @Deprecated
  public Todo_Relation orderByContentAsc() {
    return orderBy(schema.content.orderInAscending());
  }

  @Deprecated
  public Todo_Relation orderByContentDesc() {
    return orderBy(schema.content.orderInDescending());
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

  @Deprecated
  public Todo_Relation orderByIdAsc() {
    return orderBy(schema.id.orderInAscending());
  }

  @Deprecated
  public Todo_Relation orderByIdDesc() {
    return orderBy(schema.id.orderInDescending());
  }
}
