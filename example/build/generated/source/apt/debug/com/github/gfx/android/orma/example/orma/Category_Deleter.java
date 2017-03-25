package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Category_Deleter extends Deleter<Category, Category_Deleter> {
  final Category_Schema schema;

  public Category_Deleter(OrmaConnection conn, Category_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Category_Deleter(Category_Deleter that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Category_Deleter(Category_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Category_Deleter clone() {
    return new Category_Deleter(this);
  }

  @NonNull
  @Override
  public Category_Schema getSchema() {
    return schema;
  }

  @Deprecated
  public Category_Deleter nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  @Deprecated
  public Category_Deleter nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  @Deprecated
  public Category_Deleter nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  @Deprecated
  public Category_Deleter nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  @Deprecated
  public final Category_Deleter nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  @Deprecated
  public final Category_Deleter nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Category_Deleter nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  @Deprecated
  public Category_Deleter nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  @Deprecated
  public Category_Deleter nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  @Deprecated
  public Category_Deleter nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }

  public Category_Deleter idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Category_Deleter idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Category_Deleter idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Category_Deleter idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Category_Deleter idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_Deleter idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Deleter idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Category_Deleter idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Category_Deleter idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Category_Deleter idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Category_Deleter idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }
}
