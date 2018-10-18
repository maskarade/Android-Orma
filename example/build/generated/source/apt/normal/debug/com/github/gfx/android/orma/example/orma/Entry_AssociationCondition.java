package com.github.gfx.android.orma.example.orma;

import androidx.annotation.NonNull;
import com.github.gfx.android.orma.AssociationCondition;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Entry_AssociationCondition extends AssociationCondition<Entry, Entry_AssociationCondition> {
  final Entry_Schema schema;

  public Entry_AssociationCondition(OrmaConnection conn, Entry_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Entry_AssociationCondition(Entry_AssociationCondition that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Entry_AssociationCondition(Entry_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Entry_AssociationCondition clone() {
    return new Entry_AssociationCondition(this);
  }

  @NonNull
  @Override
  public Entry_Schema getSchema() {
    return schema;
  }

  public Entry_AssociationCondition idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Entry_AssociationCondition idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Entry_AssociationCondition idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Entry_AssociationCondition idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Entry_AssociationCondition idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Entry_AssociationCondition idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Entry_AssociationCondition idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Entry_AssociationCondition idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Entry_AssociationCondition idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Entry_AssociationCondition idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Entry_AssociationCondition idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  public Entry_AssociationCondition resourceTypeAndResourceIdEq(@NonNull String resourceType,
      long resourceId) {
    return where(schema.resourceType, "=", resourceType).where(schema.resourceId, "=", resourceId);
  }
}
