package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.AssociationCondition;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Todo_AssociationCondition extends AssociationCondition<Todo, Todo_AssociationCondition> {
  final Todo_Schema schema;

  public Todo_AssociationCondition(OrmaConnection conn, Todo_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Todo_AssociationCondition(Todo_AssociationCondition that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Todo_AssociationCondition(Todo_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Todo_AssociationCondition clone() {
    return new Todo_AssociationCondition(this);
  }

  @NonNull
  @Override
  public Todo_Schema getSchema() {
    return schema;
  }

  public Todo_AssociationCondition titleEq(@NonNull String title) {
    return where(schema.title, "=", title);
  }

  public Todo_AssociationCondition titleNotEq(@NonNull String title) {
    return where(schema.title, "<>", title);
  }

  public Todo_AssociationCondition titleIn(@NonNull Collection<String> values) {
    return in(false, schema.title, values);
  }

  public Todo_AssociationCondition titleNotIn(@NonNull Collection<String> values) {
    return in(true, schema.title, values);
  }

  public final Todo_AssociationCondition titleIn(@NonNull String... values) {
    return titleIn(Arrays.asList(values));
  }

  public final Todo_AssociationCondition titleNotIn(@NonNull String... values) {
    return titleNotIn(Arrays.asList(values));
  }

  public Todo_AssociationCondition titleGlob(@NonNull String pattern) {
    return where(schema.title, "GLOB", pattern);
  }

  public Todo_AssociationCondition titleNotGlob(@NonNull String pattern) {
    return where(schema.title, "NOT GLOB", pattern);
  }

  public Todo_AssociationCondition titleLike(@NonNull String pattern) {
    return where(schema.title, "LIKE", pattern);
  }

  public Todo_AssociationCondition titleNotLike(@NonNull String pattern) {
    return where(schema.title, "NOT LIKE", pattern);
  }

  public Todo_AssociationCondition titleLt(@NonNull String title) {
    return where(schema.title, "<", title);
  }

  public Todo_AssociationCondition titleLe(@NonNull String title) {
    return where(schema.title, "<=", title);
  }

  public Todo_AssociationCondition titleGt(@NonNull String title) {
    return where(schema.title, ">", title);
  }

  public Todo_AssociationCondition titleGe(@NonNull String title) {
    return where(schema.title, ">=", title);
  }

  public Todo_AssociationCondition doneEq(boolean done) {
    return where(schema.done, "=", done);
  }

  public Todo_AssociationCondition doneNotEq(boolean done) {
    return where(schema.done, "<>", done);
  }

  public Todo_AssociationCondition doneIn(@NonNull Collection<Boolean> values) {
    return in(false, schema.done, values);
  }

  public Todo_AssociationCondition doneNotIn(@NonNull Collection<Boolean> values) {
    return in(true, schema.done, values);
  }

  public final Todo_AssociationCondition doneIn(@NonNull Boolean... values) {
    return doneIn(Arrays.asList(values));
  }

  public final Todo_AssociationCondition doneNotIn(@NonNull Boolean... values) {
    return doneNotIn(Arrays.asList(values));
  }

  public Todo_AssociationCondition doneLt(boolean done) {
    return where(schema.done, "<", done);
  }

  public Todo_AssociationCondition doneLe(boolean done) {
    return where(schema.done, "<=", done);
  }

  public Todo_AssociationCondition doneGt(boolean done) {
    return where(schema.done, ">", done);
  }

  public Todo_AssociationCondition doneGe(boolean done) {
    return where(schema.done, ">=", done);
  }

  public Todo_AssociationCondition idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Todo_AssociationCondition idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Todo_AssociationCondition idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Todo_AssociationCondition idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Todo_AssociationCondition idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Todo_AssociationCondition idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Todo_AssociationCondition idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Todo_AssociationCondition idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Todo_AssociationCondition idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Todo_AssociationCondition idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Todo_AssociationCondition idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }
}
