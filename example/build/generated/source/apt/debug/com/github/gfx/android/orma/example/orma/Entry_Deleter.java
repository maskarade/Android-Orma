package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Entry_Deleter extends Deleter<Entry, Entry_Deleter> {
  final Entry_Schema schema;

  public Entry_Deleter(OrmaConnection conn, Entry_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Entry_Deleter(Entry_Deleter that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Entry_Deleter(Entry_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Entry_Deleter clone() {
    return new Entry_Deleter(this);
  }

  @NonNull
  @Override
  public Entry_Schema getSchema() {
    return schema;
  }

  @Deprecated
  public Entry_Deleter resourceTypeEq(@NonNull String resourceType) {
    return where(schema.resourceType, "=", resourceType);
  }

  @Deprecated
  public Entry_Deleter resourceTypeNotEq(@NonNull String resourceType) {
    return where(schema.resourceType, "<>", resourceType);
  }

  @Deprecated
  public Entry_Deleter resourceTypeIn(@NonNull Collection<String> values) {
    return in(false, schema.resourceType, values);
  }

  @Deprecated
  public Entry_Deleter resourceTypeNotIn(@NonNull Collection<String> values) {
    return in(true, schema.resourceType, values);
  }

  @Deprecated
  public final Entry_Deleter resourceTypeIn(@NonNull String... values) {
    return resourceTypeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Entry_Deleter resourceTypeNotIn(@NonNull String... values) {
    return resourceTypeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Entry_Deleter resourceTypeLt(@NonNull String resourceType) {
    return where(schema.resourceType, "<", resourceType);
  }

  @Deprecated
  public Entry_Deleter resourceTypeLe(@NonNull String resourceType) {
    return where(schema.resourceType, "<=", resourceType);
  }

  @Deprecated
  public Entry_Deleter resourceTypeGt(@NonNull String resourceType) {
    return where(schema.resourceType, ">", resourceType);
  }

  @Deprecated
  public Entry_Deleter resourceTypeGe(@NonNull String resourceType) {
    return where(schema.resourceType, ">=", resourceType);
  }

  @Deprecated
  public Entry_Deleter resourceIdEq(long resourceId) {
    return where(schema.resourceId, "=", resourceId);
  }

  @Deprecated
  public Entry_Deleter resourceIdNotEq(long resourceId) {
    return where(schema.resourceId, "<>", resourceId);
  }

  @Deprecated
  public Entry_Deleter resourceIdIn(@NonNull Collection<Long> values) {
    return in(false, schema.resourceId, values);
  }

  @Deprecated
  public Entry_Deleter resourceIdNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.resourceId, values);
  }

  @Deprecated
  public final Entry_Deleter resourceIdIn(@NonNull Long... values) {
    return resourceIdIn(Arrays.asList(values));
  }

  @Deprecated
  public final Entry_Deleter resourceIdNotIn(@NonNull Long... values) {
    return resourceIdNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Entry_Deleter resourceIdLt(long resourceId) {
    return where(schema.resourceId, "<", resourceId);
  }

  @Deprecated
  public Entry_Deleter resourceIdLe(long resourceId) {
    return where(schema.resourceId, "<=", resourceId);
  }

  @Deprecated
  public Entry_Deleter resourceIdGt(long resourceId) {
    return where(schema.resourceId, ">", resourceId);
  }

  @Deprecated
  public Entry_Deleter resourceIdGe(long resourceId) {
    return where(schema.resourceId, ">=", resourceId);
  }

  /**
   * To build a condition <code>resourceId BETWEEN a AND b</code>, which is equivalent to <code>a <= resourceId AND resourceId <= b</code>.
   */
  @Deprecated
  public Entry_Deleter resourceIdBetween(long resourceIdA, long resourceIdB) {
    return whereBetween(schema.resourceId, resourceIdA, resourceIdB);
  }

  public Entry_Deleter idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Entry_Deleter idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Entry_Deleter idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Entry_Deleter idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Entry_Deleter idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Entry_Deleter idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Entry_Deleter idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Entry_Deleter idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Entry_Deleter idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Entry_Deleter idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Entry_Deleter idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  public Entry_Deleter resourceTypeAndResourceIdEq(@NonNull String resourceType, long resourceId) {
    return where(schema.resourceType, "=", resourceType).where(schema.resourceId, "=", resourceId);
  }
}
