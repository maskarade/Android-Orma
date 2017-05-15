package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import java.util.Arrays;
import java.util.Collection;

public class Item_Selector extends Selector<Item, Item_Selector> {
  final Item_Schema schema;

  public Item_Selector(OrmaConnection conn, Item_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item_Selector(Item_Selector that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item_Selector(Item_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item_Selector clone() {
    return new Item_Selector(this);
  }

  @NonNull
  @Override
  public Item_Schema getSchema() {
    return schema;
  }

  public Item_Selector categoryEq(@NonNull Category category) {
    return where(schema.category, "=", category.id);
  }

  public Item_Selector categoryEq(long categoryId) {
    return where(schema.category, "=", categoryId);
  }

  public Item_Selector nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item_Selector nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item_Selector nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item_Selector nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item_Selector nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_Selector nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_Selector nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item_Selector nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item_Selector nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item_Selector nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }

  public Item_Selector orderByCategoryAsc() {
    return orderBy(schema.category.orderInAscending());
  }

  public Item_Selector orderByCategoryDesc() {
    return orderBy(schema.category.orderInDescending());
  }

  public Item_Selector orderByNameAsc() {
    return orderBy(schema.name.orderInAscending());
  }

  public Item_Selector orderByNameDesc() {
    return orderBy(schema.name.orderInDescending());
  }
}
