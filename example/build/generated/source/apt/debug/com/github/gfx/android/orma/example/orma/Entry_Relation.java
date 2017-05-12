package com.github.gfx.android.orma.example.orma;

import android.content.ContentValues;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.annotation.OnConflict;
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
  public Entry upsertWithoutTransaction(@NonNull Entry model) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("`resourceType`", model.resourceType);
    contentValues.put("`resourceId`", model.resourceId);
    if (model.id != 0) {
      int updatedRows = updater().idEq(model.id).putAll(contentValues).execute();
      if (updatedRows != 0) {
        return selector().idEq(model.id).value();
      }
    }
    long rowId = conn.insert(schema, contentValues, OnConflict.NONE);
    return conn.findByRowId(schema, rowId);
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

  @Deprecated
  public Entry_Relation resourceTypeEq(@NonNull String resourceType) {
    return where(schema.resourceType, "=", resourceType);
  }

  @Deprecated
  public Entry_Relation resourceTypeNotEq(@NonNull String resourceType) {
    return where(schema.resourceType, "<>", resourceType);
  }

  @Deprecated
  public Entry_Relation resourceTypeIn(@NonNull Collection<String> values) {
    return in(false, schema.resourceType, values);
  }

  @Deprecated
  public Entry_Relation resourceTypeNotIn(@NonNull Collection<String> values) {
    return in(true, schema.resourceType, values);
  }

  @Deprecated
  public final Entry_Relation resourceTypeIn(@NonNull String... values) {
    return resourceTypeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Entry_Relation resourceTypeNotIn(@NonNull String... values) {
    return resourceTypeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Entry_Relation resourceTypeLt(@NonNull String resourceType) {
    return where(schema.resourceType, "<", resourceType);
  }

  @Deprecated
  public Entry_Relation resourceTypeLe(@NonNull String resourceType) {
    return where(schema.resourceType, "<=", resourceType);
  }

  @Deprecated
  public Entry_Relation resourceTypeGt(@NonNull String resourceType) {
    return where(schema.resourceType, ">", resourceType);
  }

  @Deprecated
  public Entry_Relation resourceTypeGe(@NonNull String resourceType) {
    return where(schema.resourceType, ">=", resourceType);
  }

  @Deprecated
  public Entry_Relation resourceIdEq(long resourceId) {
    return where(schema.resourceId, "=", resourceId);
  }

  @Deprecated
  public Entry_Relation resourceIdNotEq(long resourceId) {
    return where(schema.resourceId, "<>", resourceId);
  }

  @Deprecated
  public Entry_Relation resourceIdIn(@NonNull Collection<Long> values) {
    return in(false, schema.resourceId, values);
  }

  @Deprecated
  public Entry_Relation resourceIdNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.resourceId, values);
  }

  @Deprecated
  public final Entry_Relation resourceIdIn(@NonNull Long... values) {
    return resourceIdIn(Arrays.asList(values));
  }

  @Deprecated
  public final Entry_Relation resourceIdNotIn(@NonNull Long... values) {
    return resourceIdNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Entry_Relation resourceIdLt(long resourceId) {
    return where(schema.resourceId, "<", resourceId);
  }

  @Deprecated
  public Entry_Relation resourceIdLe(long resourceId) {
    return where(schema.resourceId, "<=", resourceId);
  }

  @Deprecated
  public Entry_Relation resourceIdGt(long resourceId) {
    return where(schema.resourceId, ">", resourceId);
  }

  @Deprecated
  public Entry_Relation resourceIdGe(long resourceId) {
    return where(schema.resourceId, ">=", resourceId);
  }

  /**
   * To build a condition <code>resourceId BETWEEN a AND b</code>, which is equivalent to <code>a <= resourceId AND resourceId <= b</code>.
   */
  @Deprecated
  public Entry_Relation resourceIdBetween(long resourceIdA, long resourceIdB) {
    return whereBetween(schema.resourceId, resourceIdA, resourceIdB);
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

  @Deprecated
  public Entry_Relation orderByResourceTypeAsc() {
    return orderBy(schema.resourceType.orderInAscending());
  }

  @Deprecated
  public Entry_Relation orderByResourceTypeDesc() {
    return orderBy(schema.resourceType.orderInDescending());
  }

  @Deprecated
  public Entry_Relation orderByResourceIdAsc() {
    return orderBy(schema.resourceId.orderInAscending());
  }

  @Deprecated
  public Entry_Relation orderByResourceIdDesc() {
    return orderBy(schema.resourceId.orderInDescending());
  }

  @Deprecated
  public Entry_Relation orderByIdAsc() {
    return orderBy(schema.id.orderInAscending());
  }

  @Deprecated
  public Entry_Relation orderByIdDesc() {
    return orderBy(schema.id.orderInDescending());
  }

  public Entry_Relation orderByresourceTypeAndResourceIdAsc() {
    return orderBy(schema.resourceType.orderInAscending()).orderBy(schema.resourceId.orderInAscending());
  }

  public Entry_Relation orderByresourceTypeAndResourceIdDesc() {
    return orderBy(schema.resourceType.orderInDescending()).orderBy(schema.resourceId.orderInDescending());
  }
}
