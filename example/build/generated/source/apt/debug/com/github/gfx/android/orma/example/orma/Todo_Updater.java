package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Updater;
import java.lang.Boolean;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import rx.functions.Func1;

public class Todo_Updater extends Updater<Todo, Todo_Updater> {
  public Todo_Updater(OrmaConnection conn, Schema<Todo> schema) {
    super(conn, schema);
  }

  public Todo_Updater(Todo_Relation relation) {
    super(relation);
  }

  public Todo_Updater title(@NonNull String title) {
    contents.put("\"title\"", title);
    return this;
  }

  public Todo_Updater content(@Nullable String content) {
    contents.put("\"content\"", content);
    return this;
  }

  public Todo_Updater done(boolean done) {
    contents.put("\"done\"", done);
    return this;
  }

  public Todo_Updater createdTime(@NonNull Date createdTime) {
    contents.put("\"createdTime\"", BuiltInSerializers.serializeDate(createdTime));
    return this;
  }

  public Todo_Updater titleEq(@NonNull String title) {
    return where("\"title\" = ?", title);
  }

  public Todo_Updater titleNotEq(@NonNull String title) {
    return where("\"title\" <> ?", title);
  }

  public Todo_Updater titleIn(@NonNull Collection<String> values) {
    return in(false, "\"title\"", values);
  }

  public Todo_Updater titleNotIn(@NonNull Collection<String> values) {
    return in(true, "\"title\"", values);
  }

  public final Todo_Updater titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_Updater titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Updater titleLt(@NonNull String title) {
    return where("\"title\" < ?", title);
  }

  public Todo_Updater titleLe(@NonNull String title) {
    return where("\"title\" <= ?", title);
  }

  public Todo_Updater titleGt(@NonNull String title) {
    return where("\"title\" > ?", title);
  }

  public Todo_Updater titleGe(@NonNull String title) {
    return where("\"title\" >= ?", title);
  }

  public Todo_Updater doneEq(boolean done) {
    return where("\"done\" = ?", done);
  }

  public Todo_Updater doneNotEq(boolean done) {
    return where("\"done\" <> ?", done);
  }

  public Todo_Updater doneIn(@NonNull Collection<Boolean> values) {
    return in(false, "\"done\"", values);
  }

  public Todo_Updater doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, "\"done\"", values);
  }

  public final Todo_Updater doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_Updater doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Updater doneLt(boolean done) {
    return where("\"done\" < ?", done);
  }

  public Todo_Updater doneLe(boolean done) {
    return where("\"done\" <= ?", done);
  }

  public Todo_Updater doneGt(boolean done) {
    return where("\"done\" > ?", done);
  }

  public Todo_Updater doneGe(boolean done) {
    return where("\"done\" >= ?", done);
  }

  public Todo_Updater createdTimeEq(@NonNull Date createdTime) {
    return where("\"createdTime\" = ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Updater createdTimeNotEq(@NonNull Date createdTime) {
    return where("\"createdTime\" <> ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Updater createdTimeIn(@NonNull Collection<Date> values) {
    return in(false, "\"createdTime\"", values, new Func1<Date, Long>() {
      @Override
      public Long call(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  public Todo_Updater createdTimeNotIn(@NonNull Collection<Date> values) {
    return in(true, "\"createdTime\"", values, new Func1<Date, Long>() {
      @Override
      public Long call(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  public final Todo_Updater createdTimeIn(@NonNull Date... values) {
    return createdTimeIn(Arrays.asList(values));
  }

  public final Todo_Updater createdTimeNotIn(@NonNull Date... values) {
    return createdTimeNotIn(Arrays.asList(values));
  }

  public Todo_Updater createdTimeLt(@NonNull Date createdTime) {
    return where("\"createdTime\" < ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Updater createdTimeLe(@NonNull Date createdTime) {
    return where("\"createdTime\" <= ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Updater createdTimeGt(@NonNull Date createdTime) {
    return where("\"createdTime\" > ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Updater createdTimeGe(@NonNull Date createdTime) {
    return where("\"createdTime\" >= ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Updater idEq(long id) {
    return where("\"id\" = ?", id);
  }

  public Todo_Updater idNotEq(long id) {
    return where("\"id\" <> ?", id);
  }

  public Todo_Updater idIn(@NonNull Collection<Long> values) {
    return in(false, "\"id\"", values);
  }

  public Todo_Updater idNotIn(@NonNull Collection<Long> values) {
    return in(true, "\"id\"", values);
  }

  public final Todo_Updater idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_Updater idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Updater idLt(long id) {
    return where("\"id\" < ?", id);
  }

  public Todo_Updater idLe(long id) {
    return where("\"id\" <= ?", id);
  }

  public Todo_Updater idGt(long id) {
    return where("\"id\" > ?", id);
  }

  public Todo_Updater idGe(long id) {
    return where("\"id\" >= ?", id);
  }
}
