package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Item_Deleter extends Deleter<Item, Item_Deleter> {
  final Item_Schema schema;

  public Item_Deleter(OrmaConnection conn, Item_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item_Deleter(Item_Relation relation) {
    super(relation);
    this.schema = (Item_Schema) relation.getSchema();
  }

  @Override
  @NonNull
  public Item_Schema getSchema() {
    return schema;
  }

  public Item_Deleter categoryEq(@NonNull Category category) {
    return where("`Item`.`category` = ?", category.id /* primary key */);
  }

  public Item_Deleter categoryEq(long categoryId) {
    return where("`Item`.`category` = ?", categoryId);
  }

  public Item_Deleter nameEq(@NonNull String name) {
    return where("`Item`.`name` = ?", name);
  }

  public Item_Deleter nameNotEq(@NonNull String name) {
    return where("`Item`.`name` <> ?", name);
  }

  public Item_Deleter nameIn(@NonNull Collection<String> values) {
    return in(false, "`Item`.`name`", values);
  }

  public Item_Deleter nameNotIn(@NonNull Collection<String> values) {
    return in(true, "`Item`.`name`", values);
  }

  public final Item_Deleter nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_Deleter nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_Deleter nameLt(@NonNull String name) {
    return where("`Item`.`name` < ?", name);
  }

  public Item_Deleter nameLe(@NonNull String name) {
    return where("`Item`.`name` <= ?", name);
  }

  public Item_Deleter nameGt(@NonNull String name) {
    return where("`Item`.`name` > ?", name);
  }

  public Item_Deleter nameGe(@NonNull String name) {
    return where("`Item`.`name` >= ?", name);
  }
}
