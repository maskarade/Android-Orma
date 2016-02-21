package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.internal.OrmaConditionBase;
import java.lang.Boolean;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import rx.functions.Func1;

public class Todo_Selector extends Selector<Todo, Todo_Selector> {
  public Todo_Selector(OrmaConnection conn, Schema<Todo> schema) {
    super(conn, schema);
  }

  public Todo_Selector(OrmaConditionBase<Todo, ?> condition) {
    super(condition);
  }

  @Override
  public Todo_Selector clone() {
    return new Todo_Selector(this);
  }

  public Todo_Selector titleEq(@NonNull String title) {
    return where("\"title\" = ?", title);
  }

  public Todo_Selector titleNotEq(@NonNull String title) {
    return where("\"title\" <> ?", title);
  }

  public Todo_Selector titleIn(@NonNull Collection<String> values) {
    return in(false, "\"title\"", values);
  }

  public Todo_Selector titleNotIn(@NonNull Collection<String> values) {
    return in(true, "\"title\"", values);
  }

  public Todo_Selector titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public Todo_Selector titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Selector titleLt(@NonNull String title) {
    return where("\"title\" < ?", title);
  }

  public Todo_Selector titleLe(@NonNull String title) {
    return where("\"title\" <= ?", title);
  }

  public Todo_Selector titleGt(@NonNull String title) {
    return where("\"title\" > ?", title);
  }

  public Todo_Selector titleGe(@NonNull String title) {
    return where("\"title\" >= ?", title);
  }

  public Todo_Selector doneEq(boolean done) {
    return where("\"done\" = ?", done);
  }

  public Todo_Selector doneNotEq(boolean done) {
    return where("\"done\" <> ?", done);
  }

  public Todo_Selector doneIn(@NonNull Collection<Boolean> values) {
    return in(false, "\"done\"", values);
  }

  public Todo_Selector doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, "\"done\"", values);
  }

  public Todo_Selector doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public Todo_Selector doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Selector doneLt(boolean done) {
    return where("\"done\" < ?", done);
  }

  public Todo_Selector doneLe(boolean done) {
    return where("\"done\" <= ?", done);
  }

  public Todo_Selector doneGt(boolean done) {
    return where("\"done\" > ?", done);
  }

  public Todo_Selector doneGe(boolean done) {
    return where("\"done\" >= ?", done);
  }

  public Todo_Selector createdTimeEq(@NonNull Date createdTime) {
    return where("\"createdTime\" = ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Selector createdTimeNotEq(@NonNull Date createdTime) {
    return where("\"createdTime\" <> ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Selector createdTimeIn(@NonNull Collection<Date> values) {
    return in(false, "\"createdTime\"", values, new Func1<Date, Long>() {
      @Override
      public Long call(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  public Todo_Selector createdTimeNotIn(@NonNull Collection<Date> values) {
    return in(true, "\"createdTime\"", values, new Func1<Date, Long>() {
      @Override
      public Long call(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  public Todo_Selector createdTimeIn(@NonNull Date... values) {
    return createdTimeIn(Arrays.asList(values));
  }

  public Todo_Selector createdTimeNotIn(@NonNull Date... values) {
    return createdTimeNotIn(Arrays.asList(values));
  }

  public Todo_Selector createdTimeLt(@NonNull Date createdTime) {
    return where("\"createdTime\" < ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Selector createdTimeLe(@NonNull Date createdTime) {
    return where("\"createdTime\" <= ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Selector createdTimeGt(@NonNull Date createdTime) {
    return where("\"createdTime\" > ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Selector createdTimeGe(@NonNull Date createdTime) {
    return where("\"createdTime\" >= ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Selector idEq(long id) {
    return where("\"id\" = ?", id);
  }

  public Todo_Selector idNotEq(long id) {
    return where("\"id\" <> ?", id);
  }

  public Todo_Selector idIn(@NonNull Collection<Long> values) {
    return in(false, "\"id\"", values);
  }

  public Todo_Selector idNotIn(@NonNull Collection<Long> values) {
    return in(true, "\"id\"", values);
  }

  public Todo_Selector idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public Todo_Selector idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Selector idLt(long id) {
    return where("\"id\" < ?", id);
  }

  public Todo_Selector idLe(long id) {
    return where("\"id\" <= ?", id);
  }

  public Todo_Selector idGt(long id) {
    return where("\"id\" > ?", id);
  }

  public Todo_Selector idGe(long id) {
    return where("\"id\" >= ?", id);
  }

  public Todo_Selector orderByTitleAsc() {
    return orderBy("\"title\" ASC");
  }

  public Todo_Selector orderByTitleDesc() {
    return orderBy("\"title\" DESC");
  }

  public Todo_Selector orderByDoneAsc() {
    return orderBy("\"done\" ASC");
  }

  public Todo_Selector orderByDoneDesc() {
    return orderBy("\"done\" DESC");
  }

  public Todo_Selector orderByCreatedTimeAsc() {
    return orderBy("\"createdTime\" ASC");
  }

  public Todo_Selector orderByCreatedTimeDesc() {
    return orderBy("\"createdTime\" DESC");
  }
}
