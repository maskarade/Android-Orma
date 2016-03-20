package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Updater;
import java.util.Arrays;
import java.util.Collection;

public class Category_Updater extends Updater<Category, Category_Updater> {
  public Category_Updater(OrmaConnection conn, Schema<Category> schema) {
    super(conn, schema);
  }

  public Category_Updater(Category_Relation relation) {
    super(relation);
  }

  public Category_Updater name(@NonNull String name) {
    contents.put("`name`", name);
    return this;
  }

  public Category_Updater idEq(long id) {
    return where("`id` = ?", id);
  }

  public Category_Updater idNotEq(long id) {
    return where("`id` <> ?", id);
  }

  public Category_Updater idIn(@NonNull Collection<Long> values) {
    return in(false, "`id`", values);
  }

  public Category_Updater idNotIn(@NonNull Collection<Long> values) {
    return in(true, "`id`", values);
  }

  public final Category_Updater idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_Updater idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Updater idLt(long id) {
    return where("`id` < ?", id);
  }

  public Category_Updater idLe(long id) {
    return where("`id` <= ?", id);
  }

  public Category_Updater idGt(long id) {
    return where("`id` > ?", id);
  }

  public Category_Updater idGe(long id) {
    return where("`id` >= ?", id);
  }
}
