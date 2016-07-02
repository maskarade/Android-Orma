package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;

public class Item_Selector extends Selector<Item, Item_Selector> {
  public Item_Selector(OrmaConnection conn, Schema<Item> schema) {
    super(conn, schema);
  }

  public Item_Selector(OrmaConditionBase<Item, ?> condition) {
    super(condition);
  }

  @Override
  public Item_Selector clone() {
    return new Item_Selector(this);
  }

  public Item_Selector categoryEq(@NonNull Category category) {
    return where("`Item`.`category` = ?", category.id /* primary key */);
  }

  public Item_Selector categoryEq(long categoryId) {
    return where("`Item`.`category` = ?", categoryId);
  }

  public Item_Selector nameEq(@NonNull String name) {
    return where("`Item`.`name` = ?", name);
  }

  public Item_Selector nameNotEq(@NonNull String name) {
    return where("`Item`.`name` <> ?", name);
  }

  public Item_Selector nameIn(@NonNull Collection<String> values) {
    return in(false, "`Item`.`name`", values);
  }

  public Item_Selector nameNotIn(@NonNull Collection<String> values) {
    return in(true, "`Item`.`name`", values);
  }

  public final Item_Selector nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_Selector nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_Selector nameLt(@NonNull String name) {
    return where("`Item`.`name` < ?", name);
  }

  public Item_Selector nameLe(@NonNull String name) {
    return where("`Item`.`name` <= ?", name);
  }

  public Item_Selector nameGt(@NonNull String name) {
    return where("`Item`.`name` > ?", name);
  }

  public Item_Selector nameGe(@NonNull String name) {
    return where("`Item`.`name` >= ?", name);
  }

  public Item_Selector orderByCategoryAsc() {
    return orderBy(Item_Schema.category.orderInAscending());
  }

  public Item_Selector orderByCategoryDesc() {
    return orderBy(Item_Schema.category.orderInDescending());
  }

  public Item_Selector orderByNameAsc() {
    return orderBy(Item_Schema.name.orderInAscending());
  }

  public Item_Selector orderByNameDesc() {
    return orderBy(Item_Schema.name.orderInDescending());
  }
}
