package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import java.util.Arrays;
import java.util.Collection;

public class Category_Selector extends Selector<Category, Category_Selector> {
  final Category_Schema schema;

  public Category_Selector(OrmaConnection conn, Category_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Category_Selector(Category_Selector that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Category_Selector(Category_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Category_Selector clone() {
    return new Category_Selector(this);
  }

  @NonNull
  @Override
  public Category_Schema getSchema() {
    return schema;
  }

  @Deprecated
  public Category_Selector nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  @Deprecated
  public Category_Selector nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  @Deprecated
  public Category_Selector nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  @Deprecated
  public Category_Selector nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  @Deprecated
  public final Category_Selector nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  @Deprecated
  public final Category_Selector nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Category_Selector nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  @Deprecated
  public Category_Selector nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  @Deprecated
  public Category_Selector nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  @Deprecated
  public Category_Selector nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }

  public Category_Selector idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Category_Selector idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Category_Selector idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Category_Selector idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Category_Selector idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_Selector idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Selector idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Category_Selector idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Category_Selector idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Category_Selector idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Category_Selector idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  @Deprecated
  public Category_Selector orderByNameAsc() {
    return orderBy(schema.name.orderInAscending());
  }

  @Deprecated
  public Category_Selector orderByNameDesc() {
    return orderBy(schema.name.orderInDescending());
  }

  @Deprecated
  public Category_Selector orderByIdAsc() {
    return orderBy(schema.id.orderInAscending());
  }

  @Deprecated
  public Category_Selector orderByIdDesc() {
    return orderBy(schema.id.orderInDescending());
  }
}
