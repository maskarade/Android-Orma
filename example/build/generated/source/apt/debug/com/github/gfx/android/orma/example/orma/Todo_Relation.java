package com.github.gfx.android.orma.example.orma;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import java.util.Arrays;
import java.util.Collection;

public class Todo_Relation extends Relation<Todo, Todo_Relation> {
  final Todo_Schema schema;

  public Todo_Relation(OrmaConnection conn, Todo_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Todo_Relation(Todo_Relation relation) {
    super(relation);
    this.schema = (Todo_Schema) relation.getSchema();
  }

  @Override
  public Todo_Relation clone() {
    return new Todo_Relation(this);
  }

  @Override
  @NonNull
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
    return where("`title` = ?", title);
  }

  public Todo_Relation titleNotEq(@NonNull String title) {
    return where("`title` <> ?", title);
  }

  public Todo_Relation titleIn(@NonNull Collection<String> values) {
    return in(false, "`title`", values);
  }

  public Todo_Relation titleNotIn(@NonNull Collection<String> values) {
    return in(true, "`title`", values);
  }

  public final Todo_Relation titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_Relation titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_Relation titleLt(@NonNull String title) {
    return where("`title` < ?", title);
  }

  public Todo_Relation titleLe(@NonNull String title) {
    return where("`title` <= ?", title);
  }

  public Todo_Relation titleGt(@NonNull String title) {
    return where("`title` > ?", title);
  }

  public Todo_Relation titleGe(@NonNull String title) {
    return where("`title` >= ?", title);
  }

  public Todo_Relation doneEq(boolean done) {
    return where("`done` = ?", done);
  }

  public Todo_Relation doneNotEq(boolean done) {
    return where("`done` <> ?", done);
  }

  public Todo_Relation doneIn(@NonNull Collection<Boolean> values) {
    return in(false, "`done`", values);
  }

  public Todo_Relation doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, "`done`", values);
  }

  public final Todo_Relation doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_Relation doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_Relation doneLt(boolean done) {
    return where("`done` < ?", done);
  }

  public Todo_Relation doneLe(boolean done) {
    return where("`done` <= ?", done);
  }

  public Todo_Relation doneGt(boolean done) {
    return where("`done` > ?", done);
  }

  public Todo_Relation doneGe(boolean done) {
    return where("`done` >= ?", done);
  }

  public Todo_Relation idEq(long id) {
    return where("`id` = ?", id);
  }

  public Todo_Relation idNotEq(long id) {
    return where("`id` <> ?", id);
  }

  public Todo_Relation idIn(@NonNull Collection<Long> values) {
    return in(false, "`id`", values);
  }

  public Todo_Relation idNotIn(@NonNull Collection<Long> values) {
    return in(true, "`id`", values);
  }

  public final Todo_Relation idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_Relation idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_Relation idLt(long id) {
    return where("`id` < ?", id);
  }

  public Todo_Relation idLe(long id) {
    return where("`id` <= ?", id);
  }

  public Todo_Relation idGt(long id) {
    return where("`id` > ?", id);
  }

  public Todo_Relation idGe(long id) {
    return where("`id` >= ?", id);
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
