package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.internal.OrmaConditionBase;
import java.util.Arrays;
import java.util.Collection;

public class Todo_Selector extends Selector<Todo, Todo_Selector> {
  final Todo_Schema schema;

  public Todo_Selector(OrmaConnection conn, Todo_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Todo_Selector(OrmaConditionBase<Todo, ?> condition) {
    super(condition);
    this.schema = (Todo_Schema) condition.getSchema();
  }

  @Override
  public Todo_Selector clone() {
    return new Todo_Selector(this);
  }

  @Override
  @NonNull
  public Todo_Schema getSchema() {
    return schema;
  }

  public Todo_Selector titleEq(@NonNull String title) {
    return where("`title` = ?", title);
  }

  public Todo_Selector titleNotEq(@NonNull String title) {
    return where("`title` <> ?", title);
  }

  public Todo_Selector titleIn(@NonNull Collection<String> values) {
    return in(false, "`title`", values);
  }

  public Todo_Selector titleNotIn(@NonNull Collection<String> values) {
    return in(true, "`title`", values);
  }

  public final Todo_Selector titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_Selector titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Selector titleLt(@NonNull String title) {
    return where("`title` < ?", title);
  }

  public Todo_Selector titleLe(@NonNull String title) {
    return where("`title` <= ?", title);
  }

  public Todo_Selector titleGt(@NonNull String title) {
    return where("`title` > ?", title);
  }

  public Todo_Selector titleGe(@NonNull String title) {
    return where("`title` >= ?", title);
  }

  public Todo_Selector doneEq(boolean done) {
    return where("`done` = ?", done);
  }

  public Todo_Selector doneNotEq(boolean done) {
    return where("`done` <> ?", done);
  }

  public Todo_Selector doneIn(@NonNull Collection<Boolean> values) {
    return in(false, "`done`", values);
  }

  public Todo_Selector doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, "`done`", values);
  }

  public final Todo_Selector doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_Selector doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Selector doneLt(boolean done) {
    return where("`done` < ?", done);
  }

  public Todo_Selector doneLe(boolean done) {
    return where("`done` <= ?", done);
  }

  public Todo_Selector doneGt(boolean done) {
    return where("`done` > ?", done);
  }

  public Todo_Selector doneGe(boolean done) {
    return where("`done` >= ?", done);
  }

  public Todo_Selector idEq(long id) {
    return where("`id` = ?", id);
  }

  public Todo_Selector idNotEq(long id) {
    return where("`id` <> ?", id);
  }

  public Todo_Selector idIn(@NonNull Collection<Long> values) {
    return in(false, "`id`", values);
  }

  public Todo_Selector idNotIn(@NonNull Collection<Long> values) {
    return in(true, "`id`", values);
  }

  public final Todo_Selector idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_Selector idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Selector idLt(long id) {
    return where("`id` < ?", id);
  }

  public Todo_Selector idLe(long id) {
    return where("`id` <= ?", id);
  }

  public Todo_Selector idGt(long id) {
    return where("`id` > ?", id);
  }

  public Todo_Selector idGe(long id) {
    return where("`id` >= ?", id);
  }

  public Todo_Selector orderByTitleAsc() {
    return orderBy(schema.title.orderInAscending());
  }

  public Todo_Selector orderByTitleDesc() {
    return orderBy(schema.title.orderInDescending());
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
}
