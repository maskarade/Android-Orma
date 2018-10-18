package com.github.gfx.android.orma.example.orma;

import androidx.annotation.NonNull;
import com.github.gfx.android.orma.rx.RxDeleter;
import com.github.gfx.android.orma.rx.RxOrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Entry_Deleter extends RxDeleter<Entry, Entry_Deleter> {
  final Entry_Schema schema;

  public Entry_Deleter(RxOrmaConnection conn, Entry_Schema schema) {
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
