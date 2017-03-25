package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Updater;
import java.util.Arrays;
import java.util.Collection;

public class Entry_Updater extends Updater<Entry, Entry_Updater> {
  final Entry_Schema schema;

  public Entry_Updater(OrmaConnection conn, Entry_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Entry_Updater(Entry_Updater that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Entry_Updater(Entry_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Entry_Updater clone() {
    return new Entry_Updater(this);
  }

  @NonNull
  @Override
  public Entry_Schema getSchema() {
    return schema;
  }

  public Entry_Updater resourceType(@NonNull String resourceType) {
    contents.put("`resourceType`", resourceType);
    return this;
  }

  public Entry_Updater resourceId(long resourceId) {
    contents.put("`resourceId`", resourceId);
    return this;
  }

  @Deprecated
  public Entry_Updater resourceTypeEq(@NonNull String resourceType) {
    return where(schema.resourceType, "=", resourceType);
  }

  @Deprecated
  public Entry_Updater resourceTypeNotEq(@NonNull String resourceType) {
    return where(schema.resourceType, "<>", resourceType);
  }

  @Deprecated
  public Entry_Updater resourceTypeIn(@NonNull Collection<String> values) {
    return in(false, schema.resourceType, values);
  }

  @Deprecated
  public Entry_Updater resourceTypeNotIn(@NonNull Collection<String> values) {
    return in(true, schema.resourceType, values);
  }

  @Deprecated
  public final Entry_Updater resourceTypeIn(@NonNull String... values) {
    return resourceTypeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Entry_Updater resourceTypeNotIn(@NonNull String... values) {
    return resourceTypeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Entry_Updater resourceTypeLt(@NonNull String resourceType) {
    return where(schema.resourceType, "<", resourceType);
  }

  @Deprecated
  public Entry_Updater resourceTypeLe(@NonNull String resourceType) {
    return where(schema.resourceType, "<=", resourceType);
  }

  @Deprecated
  public Entry_Updater resourceTypeGt(@NonNull String resourceType) {
    return where(schema.resourceType, ">", resourceType);
  }

  @Deprecated
  public Entry_Updater resourceTypeGe(@NonNull String resourceType) {
    return where(schema.resourceType, ">=", resourceType);
  }

  @Deprecated
  public Entry_Updater resourceIdEq(long resourceId) {
    return where(schema.resourceId, "=", resourceId);
  }

  @Deprecated
  public Entry_Updater resourceIdNotEq(long resourceId) {
    return where(schema.resourceId, "<>", resourceId);
  }

  @Deprecated
  public Entry_Updater resourceIdIn(@NonNull Collection<Long> values) {
    return in(false, schema.resourceId, values);
  }

  @Deprecated
  public Entry_Updater resourceIdNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.resourceId, values);
  }

  @Deprecated
  public final Entry_Updater resourceIdIn(@NonNull Long... values) {
    return resourceIdIn(Arrays.asList(values));
  }

  @Deprecated
  public final Entry_Updater resourceIdNotIn(@NonNull Long... values) {
    return resourceIdNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Entry_Updater resourceIdLt(long resourceId) {
    return where(schema.resourceId, "<", resourceId);
  }

  @Deprecated
  public Entry_Updater resourceIdLe(long resourceId) {
    return where(schema.resourceId, "<=", resourceId);
  }

  @Deprecated
  public Entry_Updater resourceIdGt(long resourceId) {
    return where(schema.resourceId, ">", resourceId);
  }

  @Deprecated
  public Entry_Updater resourceIdGe(long resourceId) {
    return where(schema.resourceId, ">=", resourceId);
  }

  /**
   * To build a condition <code>resourceId BETWEEN a AND b</code>, which is equivalent to <code>a <= resourceId AND resourceId <= b</code>.
   */
  @Deprecated
  public Entry_Updater resourceIdBetween(long resourceIdA, long resourceIdB) {
    return whereBetween(schema.resourceId, resourceIdA, resourceIdB);
  }

  public Entry_Updater idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Entry_Updater idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Entry_Updater idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Entry_Updater idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Entry_Updater idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Entry_Updater idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Entry_Updater idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Entry_Updater idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Entry_Updater idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Entry_Updater idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Entry_Updater idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  public Entry_Updater resourceTypeAndResourceIdEq(@NonNull String resourceType, long resourceId) {
    return where(schema.resourceType, "=", resourceType).where(schema.resourceId, "=", resourceId);
  }
}
