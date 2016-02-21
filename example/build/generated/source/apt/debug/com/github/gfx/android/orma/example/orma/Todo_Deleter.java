package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import java.lang.Boolean;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import rx.functions.Func1;

public class Todo_Deleter extends Deleter<Todo, Todo_Deleter> {
  public Todo_Deleter(OrmaConnection conn, Schema<Todo> schema) {
    super(conn, schema);
  }

  public Todo_Deleter(Todo_Relation relation) {
    super(relation);
  }

  public Todo_Deleter titleEq(@NonNull String title) {
    return where("\"title\" = ?", title);
  }

  public Todo_Deleter titleNotEq(@NonNull String title) {
    return where("\"title\" <> ?", title);
  }

  public Todo_Deleter titleIn(@NonNull Collection<String> values) {
    return in(false, "\"title\"", values);
  }

  public Todo_Deleter titleNotIn(@NonNull Collection<String> values) {
    return in(true, "\"title\"", values);
  }

  public Todo_Deleter titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public Todo_Deleter titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Deleter titleLt(@NonNull String title) {
    return where("\"title\" < ?", title);
  }

  public Todo_Deleter titleLe(@NonNull String title) {
    return where("\"title\" <= ?", title);
  }

  public Todo_Deleter titleGt(@NonNull String title) {
    return where("\"title\" > ?", title);
  }

  public Todo_Deleter titleGe(@NonNull String title) {
    return where("\"title\" >= ?", title);
  }

  public Todo_Deleter doneEq(boolean done) {
    return where("\"done\" = ?", done);
  }

  public Todo_Deleter doneNotEq(boolean done) {
    return where("\"done\" <> ?", done);
  }

  public Todo_Deleter doneIn(@NonNull Collection<Boolean> values) {
    return in(false, "\"done\"", values);
  }

  public Todo_Deleter doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, "\"done\"", values);
  }

  public Todo_Deleter doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public Todo_Deleter doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Deleter doneLt(boolean done) {
    return where("\"done\" < ?", done);
  }

  public Todo_Deleter doneLe(boolean done) {
    return where("\"done\" <= ?", done);
  }

  public Todo_Deleter doneGt(boolean done) {
    return where("\"done\" > ?", done);
  }

  public Todo_Deleter doneGe(boolean done) {
    return where("\"done\" >= ?", done);
  }

  public Todo_Deleter createdTimeEq(@NonNull Date createdTime) {
    return where("\"createdTime\" = ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Deleter createdTimeNotEq(@NonNull Date createdTime) {
    return where("\"createdTime\" <> ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Deleter createdTimeIn(@NonNull Collection<Date> values) {
    return in(false, "\"createdTime\"", values, new Func1<Date, Long>() {
      @Override
      public Long call(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  public Todo_Deleter createdTimeNotIn(@NonNull Collection<Date> values) {
    return in(true, "\"createdTime\"", values, new Func1<Date, Long>() {
      @Override
      public Long call(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  public Todo_Deleter createdTimeIn(@NonNull Date... values) {
    return createdTimeIn(Arrays.asList(values));
  }

  public Todo_Deleter createdTimeNotIn(@NonNull Date... values) {
    return createdTimeNotIn(Arrays.asList(values));
  }

  public Todo_Deleter createdTimeLt(@NonNull Date createdTime) {
    return where("\"createdTime\" < ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Deleter createdTimeLe(@NonNull Date createdTime) {
    return where("\"createdTime\" <= ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Deleter createdTimeGt(@NonNull Date createdTime) {
    return where("\"createdTime\" > ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Deleter createdTimeGe(@NonNull Date createdTime) {
    return where("\"createdTime\" >= ?", BuiltInSerializers.serializeDate(createdTime));
  }

  public Todo_Deleter idEq(long id) {
    return where("\"id\" = ?", id);
  }

  public Todo_Deleter idNotEq(long id) {
    return where("\"id\" <> ?", id);
  }

  public Todo_Deleter idIn(@NonNull Collection<Long> values) {
    return in(false, "\"id\"", values);
  }

  public Todo_Deleter idNotIn(@NonNull Collection<Long> values) {
    return in(true, "\"id\"", values);
  }

  public Todo_Deleter idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public Todo_Deleter idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Deleter idLt(long id) {
    return where("\"id\" < ?", id);
  }

  public Todo_Deleter idLe(long id) {
    return where("\"id\" <= ?", id);
  }

  public Todo_Deleter idGt(long id) {
    return where("\"id\" > ?", id);
  }

  public Todo_Deleter idGe(long id) {
    return where("\"id\" >= ?", id);
  }
}
