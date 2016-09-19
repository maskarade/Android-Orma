package com.github.gfx.android.orma.example.orma;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import java.util.Arrays;
import java.util.Collection;

public class Category_Relation extends Relation<Category, Category_Relation> {
  final Category_Schema schema;

  public Category_Relation(OrmaConnection conn, Category_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Category_Relation(Category_Relation relation) {
    super(relation);
    this.schema = (Category_Schema) relation.getSchema();
  }

  @Override
  public Category_Relation clone() {
    return new Category_Relation(this);
  }

  @Override
  @NonNull
  public Category_Schema getSchema() {
    return schema;
  }

  @NonNull
  @CheckResult
  public Category reload(@NonNull Category model) {
    return selector().idEq(model.id).value();
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
    return where("`id` = ?", id);
  }

  public Category_Relation idNotEq(long id) {
    return where("`id` <> ?", id);
  }

  public Category_Relation idIn(@NonNull Collection<Long> values) {
    return in(false, "`id`", values);
  }

  public Category_Relation idNotIn(@NonNull Collection<Long> values) {
    return in(true, "`id`", values);
  }

  public final Category_Relation idIn(@NonNull Long... values) {
    return idIn(Arrays.asList(values));
  }

  public final Category_Relation idNotIn(@NonNull Long... values) {
    return idNotIn(Arrays.asList(values));
  }

  public Category_Relation idLt(long id) {
    return where("`id` < ?", id);
  }

  public Category_Relation idLe(long id) {
    return where("`id` <= ?", id);
  }

  public Category_Relation idGt(long id) {
    return where("`id` > ?", id);
  }

  public Category_Relation idGe(long id) {
    return where("`id` >= ?", id);
  }
}
