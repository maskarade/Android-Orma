package com.github.gfx.android.orma.example.orma;

import android.content.ContentValues;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.annotation.OnConflict;
import java.util.Arrays;
import java.util.Collection;

public class Item_Relation extends Relation<Item, Item_Relation> {
  final Item_Schema schema;

  public Item_Relation(OrmaConnection conn, Item_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item_Relation(Item_Relation that) {
    super(that);
    this.schema = that.getSchema();
  }

  @Override
  public Item_Relation clone() {
    return new Item_Relation(this);
  }

  @NonNull
  @Override
  public Item_Schema getSchema() {
    return schema;
  }

  @NonNull
  @CheckResult
  public Item reload(@NonNull Item model) {
    return selector().nameEq(model.name).value();
  }

  @NonNull
  @Override
  public Item upsertWithoutTransaction(@NonNull Item model) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("`category`", Category_Schema.INSTANCE.createRelation(conn).upsertWithoutTransaction(model.category).id);
    contentValues.put("`name`", model.name);
    int updatedRows = updater().nameEq(model.name).putAll(contentValues).execute();
    if (updatedRows != 0) {
      return selector().nameEq(model.name).value();
    }
    long rowId = conn.insert(schema, contentValues, OnConflict.NONE);
    return conn.findByRowId(schema, rowId);
  }

  @NonNull
  @Override
  public Item_Selector selector() {
    return new Item_Selector(this);
  }

  @NonNull
  @Override
  public Item_Updater updater() {
    return new Item_Updater(this);
  }

  @NonNull
  @Override
  public Item_Deleter deleter() {
    return new Item_Deleter(this);
  }

  public Item_Relation categoryEq(@NonNull Category category) {
    return where(schema.category, "=", category.id);
  }

  public Item_Relation categoryEq(long categoryId) {
    return where(schema.category, "=", categoryId);
  }

  public Item_Relation nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item_Relation nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item_Relation nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item_Relation nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item_Relation nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_Relation nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_Relation nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item_Relation nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item_Relation nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item_Relation nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }

  public Item_Relation orderByCategoryAsc() {
    return orderBy(schema.category.orderInAscending());
  }

  public Item_Relation orderByCategoryDesc() {
    return orderBy(schema.category.orderInDescending());
  }

  public Item_Relation orderByNameAsc() {
    return orderBy(schema.name.orderInAscending());
  }

  public Item_Relation orderByNameDesc() {
    return orderBy(schema.name.orderInDescending());
  }
}
