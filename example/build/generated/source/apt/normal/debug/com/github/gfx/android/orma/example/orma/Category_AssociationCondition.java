package com.github.gfx.android.orma.example.orma;

import androidx.annotation.NonNull;
import com.github.gfx.android.orma.AssociationCondition;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Category_AssociationCondition extends AssociationCondition<Category, Category_AssociationCondition> {
  final Category_Schema schema;

  public Category_AssociationCondition(OrmaConnection conn, Category_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Category_AssociationCondition(Category_AssociationCondition that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Category_AssociationCondition(Category_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Category_AssociationCondition clone() {
    return new Category_AssociationCondition(this);
  }

  @NonNull
  @Override
  public Category_Schema getSchema() {
    return schema;
  }

  public Category_AssociationCondition idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Category_AssociationCondition idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Category_AssociationCondition idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Category_AssociationCondition idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Category_AssociationCondition idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_AssociationCondition idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_AssociationCondition idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Category_AssociationCondition idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Category_AssociationCondition idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Category_AssociationCondition idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Category_AssociationCondition idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }
}
