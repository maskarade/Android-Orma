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

  public Category_Deleter(Category_Relation relation) {
    super(relation);
    this.schema = (Category_Schema) relation.getSchema();
  }

  @Override
  @NonNull
  public Category_Schema getSchema() {
    return schema;
  }

  public Category_Deleter idEq(long id) {
    return where("`id` = ?", id);
  }

  public Category_Deleter idNotEq(long id) {
    return where("`id` <> ?", id);
  }

  public Category_Deleter idIn(@NonNull Collection<Long> values) {
    return in(false, "`id`", values);
  }

  public Category_Deleter idNotIn(@NonNull Collection<Long> values) {
    return in(true, "`id`", values);
  }

  public final Category_Deleter idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_Deleter idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Deleter idLt(long id) {
    return where("`id` < ?", id);
  }

  public Category_Deleter idLe(long id) {
    return where("`id` <= ?", id);
  }

  public Category_Deleter idGt(long id) {
    return where("`id` > ?", id);
  }

  public Category_Deleter idGe(long id) {
    return where("`id` >= ?", id);
  }
}
