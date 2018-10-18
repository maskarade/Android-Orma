package com.github.gfx.android.orma.example.orma;

import androidx.annotation.NonNull;
import com.github.gfx.android.orma.rx.RxOrmaConnection;
import com.github.gfx.android.orma.rx.RxSelector;
import java.util.Arrays;
import java.util.Collection;

public class Category_Selector extends RxSelector<Category, Category_Selector> {
  final Category_Schema schema;

  public Category_Selector(RxOrmaConnection conn, Category_Schema schema) {
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
}
