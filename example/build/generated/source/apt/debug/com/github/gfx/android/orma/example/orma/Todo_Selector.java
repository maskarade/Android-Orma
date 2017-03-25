package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class Todo_Selector extends Selector<Todo, Todo_Selector> {
  final Todo_Schema schema;

  public Todo_Selector(OrmaConnection conn, Todo_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Todo_Selector(Todo_Selector that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Todo_Selector(Todo_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Todo_Selector clone() {
    return new Todo_Selector(this);
  }

  @NonNull
  @Override
  public Todo_Schema getSchema() {
    return schema;
  }

  public Todo_Selector titleEq(@NonNull String title) {
    return where(schema.title, "=", title);
  }

  public Todo_Selector titleNotEq(@NonNull String title) {
    return where(schema.title, "<>", title);
  }

  public Todo_Selector titleIn(@NonNull Collection<String> values) {
    return in(false, schema.title, values);
  }

  public Todo_Selector titleNotIn(@NonNull Collection<String> values) {
    return in(true, schema.title, values);
  }

  public final Todo_Selector titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_Selector titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Selector titleLt(@NonNull String title) {
    return where(schema.title, "<", title);
  }

  public Todo_Selector titleLe(@NonNull String title) {
    return where(schema.title, "<=", title);
  }

  public Todo_Selector titleGt(@NonNull String title) {
    return where(schema.title, ">", title);
  }

  public Todo_Selector titleGe(@NonNull String title) {
    return where(schema.title, ">=", title);
  }

  @Deprecated
  public Todo_Selector contentIsNull() {
    return where(schema.content, " IS NULL");
  }

  @Deprecated
  public Todo_Selector contentIsNotNull() {
    return where(schema.content, " IS NOT NULL");
  }

  @Deprecated
  public Todo_Selector contentEq(@NonNull String content) {
    return where(schema.content, "=", content);
  }

  @Deprecated
  public Todo_Selector contentNotEq(@NonNull String content) {
    return where(schema.content, "<>", content);
  }

  @Deprecated
  public Todo_Selector contentIn(@NonNull Collection<String> values) {
    return in(false, schema.content, values);
  }

  @Deprecated
  public Todo_Selector contentNotIn(@NonNull Collection<String> values) {
    return in(true, schema.content, values);
  }

  @Deprecated
  public final Todo_Selector contentIn(@NonNull String... values) {
    return contentIn(Arrays.asList(values));
  }

  @Deprecated
  public final Todo_Selector contentNotIn(@NonNull String... values) {
    return contentNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Todo_Selector contentLt(@NonNull String content) {
    return where(schema.content, "<", content);
  }

  @Deprecated
  public Todo_Selector contentLe(@NonNull String content) {
    return where(schema.content, "<=", content);
  }

  @Deprecated
  public Todo_Selector contentGt(@NonNull String content) {
    return where(schema.content, ">", content);
  }

  @Deprecated
  public Todo_Selector contentGe(@NonNull String content) {
    return where(schema.content, ">=", content);
  }

  public Todo_Selector doneEq(boolean done) {
    return where(schema.done, "=", done);
  }

  public Todo_Selector doneNotEq(boolean done) {
    return where(schema.done, "<>", done);
  }

  public Todo_Selector doneIn(@NonNull Collection<Boolean> values) {
    return in(false, schema.done, values);
  }

  public Todo_Selector doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, schema.done, values);
  }

  public final Todo_Selector doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_Selector doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Selector doneLt(boolean done) {
    return where(schema.done, "<", done);
  }

  public Todo_Selector doneLe(boolean done) {
    return where(schema.done, "<=", done);
  }

  public Todo_Selector doneGt(boolean done) {
    return where(schema.done, ">", done);
  }

  public Todo_Selector doneGe(boolean done) {
    return where(schema.done, ">=", done);
  }

  @Deprecated
  public Todo_Selector createdTimeEq(@NonNull Date createdTime) {
    return where(schema.createdTime, "=", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Selector createdTimeNotEq(@NonNull Date createdTime) {
    return where(schema.createdTime, "<>", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Selector createdTimeIn(@NonNull Collection<Date> values) {
    return in(false, schema.createdTime, values, new Function1<Date, Long>() {
      @Override
      public Long apply(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  @Deprecated
  public Todo_Selector createdTimeNotIn(@NonNull Collection<Date> values) {
    return in(true, schema.createdTime, values, new Function1<Date, Long>() {
      @Override
      public Long apply(Date value) {
        return BuiltInSerializers.serializeDate(value);
      }
    });
  }

  @Deprecated
  public final Todo_Selector createdTimeIn(@NonNull Date... values) {
    return createdTimeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Todo_Selector createdTimeNotIn(@NonNull Date... values) {
    return createdTimeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Todo_Selector createdTimeLt(@NonNull Date createdTime) {
    return where(schema.createdTime, "<", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Selector createdTimeLe(@NonNull Date createdTime) {
    return where(schema.createdTime, "<=", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Selector createdTimeGt(@NonNull Date createdTime) {
    return where(schema.createdTime, ">", BuiltInSerializers.serializeDate(createdTime));
  }

  @Deprecated
  public Todo_Selector createdTimeGe(@NonNull Date createdTime) {
    return where(schema.createdTime, ">=", BuiltInSerializers.serializeDate(createdTime));
  }

  /**
   * To build a condition <code>createdTime BETWEEN a AND b</code>, which is equivalent to <code>a <= createdTime AND createdTime <= b</code>.
   */
  @Deprecated
  public Todo_Selector createdTimeBetween(@NonNull Date createdTimeA, @NonNull Date createdTimeB) {
    return whereBetween(schema.createdTime, BuiltInSerializers.serializeDate(createdTimeA), BuiltInSerializers.serializeDate(createdTimeB));
  }

  public Todo_Selector idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Todo_Selector idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Todo_Selector idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Todo_Selector idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Todo_Selector idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_Selector idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Selector idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Todo_Selector idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Todo_Selector idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Todo_Selector idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Todo_Selector idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  public Todo_Selector orderByTitleAsc() {
    return orderBy(schema.title.orderInAscending());
  }

  public Todo_Selector orderByTitleDesc() {
    return orderBy(schema.title.orderInDescending());
  }

  @Deprecated
  public Todo_Selector orderByContentAsc() {
    return orderBy(schema.content.orderInAscending());
  }

  @Deprecated
  public Todo_Selector orderByContentDesc() {
    return orderBy(schema.content.orderInDescending());
  }

  public Todo_Selector orderByDoneAsc() {
    return orderBy(schema.done.orderInAscending());
  }

  public Todo_Selector orderByDoneDesc() {
    return orderBy(schema.done.orderInDescending());
  }

  public Todo_Selector orderByCreatedTimeAsc() {
    return orderBy(schema.createdTime.orderInAscending());
  }

  public Todo_Selector orderByCreatedTimeDesc() {
    return orderBy(schema.createdTime.orderInDescending());
  }

  @Deprecated
  public Todo_Selector orderByIdAsc() {
    return orderBy(schema.id.orderInAscending());
  }

  @Deprecated
  public Todo_Selector orderByIdDesc() {
    return orderBy(schema.id.orderInDescending());
  }
}
