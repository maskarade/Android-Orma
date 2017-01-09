package com.github.gfx.android.orma.example.orma;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import java.util.Arrays;
import java.util.Collection;

public class Entry_Relation extends Relation<Entry, Entry_Relation> {
  final Entry_Schema schema;

  public Entry_Relation(OrmaConnection conn, Entry_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Entry_Relation(Entry_Relation that) {
    super(that);
    this.schema = that.getSchema();
  }

  @Override
  public Entry_Relation clone() {
    return new Entry_Relation(this);
  }

  @NonNull
  @Override
  public Entry_Schema getSchema() {
    return schema;
  }

  @NonNull
  @CheckResult
  public Entry reload(@NonNull Entry model) {
    return selector().idEq(model.id).value();
  }

  @NonNull
  @Override
  public Entry_Selector selector() {
    return new Entry_Selector(this);
  }

  @NonNull
  @Override
  public Entry_Updater updater() {
    return new Entry_Updater(this);
  }

  @NonNull
  @Override
  public Entry_Deleter deleter() {
    return new Entry_Deleter(this);
  }

  public Entry_Relation idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Entry_Relation idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Entry_Relation idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Entry_Relation idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Entry_Relation idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Entry_Relation idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Entry_Relation idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Entry_Relation idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Entry_Relation idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Entry_Relation idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Entry_Relation idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  public Entry_Relation resourceTypeAndResourceIdEq(@NonNull String resourceType, long resourceId) {
    return where(schema.resourceType, "=", resourceType).where(schema.resourceId, "=", resourceId);
  }

  public Entry_Relation orderByresourceTypeAndResourceIdAsc() {
    return orderBy(schema.resourceType.orderInAscending()).orderBy(schema.resourceId.orderInAscending());
  }

  public Entry_Relation orderByresourceTypeAndResourceIdDesc() {
    return orderBy(schema.resourceType.orderInDescending()).orderBy(schema.resourceId.orderInDescending());
  }
}
