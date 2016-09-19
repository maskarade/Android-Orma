package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Updater;
import java.util.Arrays;
import java.util.Collection;

public class Item_Updater extends Updater<Item, Item_Updater> {
  final Item_Schema schema;

  public Item_Updater(OrmaConnection conn, Item_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item_Updater(Item_Relation relation) {
    super(relation);
    this.schema = (Item_Schema) relation.getSchema();
  }

  @Override
  @NonNull
  public Item_Schema getSchema() {
    return schema;
  }

  public Item_Updater category(@NonNull Category category) {
    contents.put("`category`", category.id);
    return this;
  }

  public Item_Updater name(@NonNull String name) {
    contents.put("`name`", name);
    return this;
  }

  public Item_Updater categoryEq(@NonNull Category category) {
    return where("`Item`.`category` = ?", category.id /* primary key */);
  }

  public Item_Updater categoryEq(long categoryId) {
    return where("`Item`.`category` = ?", categoryId);
  }

  public Item_Updater nameEq(@NonNull String name) {
    return where("`Item`.`name` = ?", name);
  }

  public Item_Updater nameNotEq(@NonNull String name) {
    return where("`Item`.`name` <> ?", name);
  }

  public Item_Updater nameIn(@NonNull Collection<String> values) {
    return in(false, "`Item`.`name`", values);
  }

  public Item_Updater nameNotIn(@NonNull Collection<String> values) {
    return in(true, "`Item`.`name`", values);
  }

  public final Item_Updater nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_Updater nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_Updater nameLt(@NonNull String name) {
    return where("`Item`.`name` < ?", name);
  }

  public Item_Updater nameLe(@NonNull String name) {
    return where("`Item`.`name` <= ?", name);
  }

  public Item_Updater nameGt(@NonNull String name) {
    return where("`Item`.`name` > ?", name);
  }

  public Item_Updater nameGe(@NonNull String name) {
    return where("`Item`.`name` >= ?", name);
  }
}
