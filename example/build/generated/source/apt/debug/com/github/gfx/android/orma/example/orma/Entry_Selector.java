package com.github.gfx.android.orma.example.orma;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import java.util.Arrays;
import java.util.Collection;

public class Entry_Selector extends Selector<Entry, Entry_Selector> {
  final Entry_Schema schema;

  public Entry_Selector(OrmaConnection conn, Entry_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Entry_Selector(Entry_Selector selector) {
    super(selector);
    this.schema = selector.getSchema();
  }

  public Entry_Selector(Entry_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Entry_Selector clone() {
    return new Entry_Selector(this);
  }

  @NonNull
  @Override
  public Entry_Schema getSchema() {
    return schema;
  }

  public Entry_Selector idEq(long id) {
    return where(schema.id, "=", id);
  }

  public Entry_Selector idNotEq(long id) {
    return where(schema.id, "<>", id);
  }

  public Entry_Selector idIn(@NonNull Collection<Long> values) {
    return in(false, schema.id, values);
  }

  public Entry_Selector idNotIn(@NonNull Collection<Long> values) {
    return in(true, schema.id, values);
  }

  public final Entry_Selector idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Entry_Selector idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Entry_Selector idLt(long id) {
    return where(schema.id, "<", id);
  }

  public Entry_Selector idLe(long id) {
    return where(schema.id, "<=", id);
  }

  public Entry_Selector idGt(long id) {
    return where(schema.id, ">", id);
  }

  public Entry_Selector idGe(long id) {
    return where(schema.id, ">=", id);
  }

  /**
   * To build a condition <code>id BETWEEN a AND b</code>, which is equivalent to <code>a <= id AND id <= b</code>.
   */
  public Entry_Selector idBetween(long idA, long idB) {
    return whereBetween(schema.id, idA, idB);
  }

  @Nullable
  public Long minByResourceId() {
    Cursor cursor = executeWithColumns(schema.resourceId.buildCallExpr("MIN"));
    try {
      cursor.moveToFirst();
      return cursor.isNull(0) ? null : schema.resourceId.getFromCursor(conn, cursor, 0);
    }
    finally {
      cursor.close();
    }
  }

  @Nullable
  public Long maxByResourceId() {
    Cursor cursor = executeWithColumns(schema.resourceId.buildCallExpr("MAX"));
    try {
      cursor.moveToFirst();
      return cursor.isNull(0) ? null : schema.resourceId.getFromCursor(conn, cursor, 0);
    }
    finally {
      cursor.close();
    }
  }

  @Nullable
  public Long sumByResourceId() {
    Cursor cursor = executeWithColumns(schema.resourceId.buildCallExpr("SUM"));
    try {
      cursor.moveToFirst();
      return cursor.isNull(0) ? null : cursor.getLong(0);
    }
    finally {
      cursor.close();
    }
  }

  @Nullable
  public Double avgByResourceId() {
    Cursor cursor = executeWithColumns(schema.resourceId.buildCallExpr("AVG"));
    try {
      cursor.moveToFirst();
      return cursor.isNull(0) ? null : cursor.getDouble(0);
    }
    finally {
      cursor.close();
    }
  }
}
