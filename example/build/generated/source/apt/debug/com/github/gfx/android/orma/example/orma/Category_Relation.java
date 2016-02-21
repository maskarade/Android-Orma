package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.Schema;
import java.lang.Long;
import java.lang.Override;
import java.util.Arrays;
import java.util.Collection;

public class Category_Relation extends Relation<Category, Category_Relation> {
  public Category_Relation(OrmaConnection conn, Schema<Category> schema) {
    super(conn, schema);
  }

  public Category_Relation(Category_Relation relation) {
    super(relation);
  }

  @Override
  public Category_Relation clone() {
    return new Category_Relation(this);
  }

  @NonNull
  @Override
  public Category_Selector selector() {
    return new Category_Selector(this);
  }

  @NonNull
  @Override
  public Category_Updater updater() {
    return new Category_Updater(this);
  }

  @NonNull
  @Override
  public Category_Deleter deleter() {
    return new Category_Deleter(this);
  }

  public Category_Relation idEq(long id) {
    return where("\"id\" = ?", id);
  }

  public Category_Relation idNotEq(long id) {
    return where("\"id\" <> ?", id);
  }

  public Category_Relation idIn(@NonNull Collection<Long> values) {
    return in(false, "\"id\"", values);
  }

  public Category_Relation idNotIn(@NonNull Collection<Long> values) {
    return in(true, "\"id\"", values);
  }

  public Category_Relation idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public Category_Relation idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Relation idLt(long id) {
    return where("\"id\" < ?", id);
  }

  public Category_Relation idLe(long id) {
    return where("\"id\" <= ?", id);
  }

  public Category_Relation idGt(long id) {
    return where("\"id\" > ?", id);
  }

  public Category_Relation idGe(long id) {
    return where("\"id\" >= ?", id);
  }
}
