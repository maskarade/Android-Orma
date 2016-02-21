package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.internal.OrmaConditionBase;
import java.lang.Long;
import java.lang.Override;
import java.util.Arrays;
import java.util.Collection;

public class Category_Selector extends Selector<Category, Category_Selector> {
  public Category_Selector(OrmaConnection conn, Schema<Category> schema) {
    super(conn, schema);
  }

  public Category_Selector(OrmaConditionBase<Category, ?> condition) {
    super(condition);
  }

  @Override
  public Category_Selector clone() {
    return new Category_Selector(this);
  }

  public Category_Selector idEq(long id) {
    return where("\"id\" = ?", id);
  }

  public Category_Selector idNotEq(long id) {
    return where("\"id\" <> ?", id);
  }

  public Category_Selector idIn(@NonNull Collection<Long> values) {
    return in(false, "\"id\"", values);
  }

  public Category_Selector idNotIn(@NonNull Collection<Long> values) {
    return in(true, "\"id\"", values);
  }

  public final Category_Selector idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_Selector idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Selector idLt(long id) {
    return where("\"id\" < ?", id);
  }

  public Category_Selector idLe(long id) {
    return where("\"id\" <= ?", id);
  }

  public Category_Selector idGt(long id) {
    return where("\"id\" > ?", id);
  }

  public Category_Selector idGe(long id) {
    return where("\"id\" >= ?", id);
  }
}
