package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.internal.OrmaConditionBase;
import java.util.Arrays;
import java.util.Collection;

public class Item2_Selector extends Selector<Item2, Item2_Selector> {
  final Item2_Schema schema;

  public Item2_Selector(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Selector(OrmaConditionBase<Item2, ?> condition) {
    super(condition);
    this.schema = (Item2_Schema) condition.getSchema();
  }

  @Override
  public Item2_Selector clone() {
    return new Item2_Selector(this);
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Selector category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id /* primary key */);
  }

  public Item2_Selector category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Selector category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_Selector category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_Selector category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id /* primary key */);
  }

  public Item2_Selector category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  public Item2_Selector nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_Selector nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_Selector nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_Selector nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_Selector nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_Selector nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_Selector nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_Selector nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_Selector nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_Selector nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }

  public Item2_Selector orderByCategory1Asc() {
    return orderBy(schema.category1.orderInAscending());
  }

  public Item2_Selector orderByCategory1Desc() {
    return orderBy(schema.category1.orderInDescending());
  }

  public Item2_Selector orderByCategory2Asc() {
    return orderBy(schema.category2.orderInAscending());
  }

  public Item2_Selector orderByCategory2Desc() {
    return orderBy(schema.category2.orderInDescending());
  }

  public Item2_Selector orderByNameAsc() {
    return orderBy(schema.name.orderInAscending());
  }

  public Item2_Selector orderByNameDesc() {
    return orderBy(schema.name.orderInDescending());
  }
}
