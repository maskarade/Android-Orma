package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Updater;
import java.util.Arrays;
import java.util.Collection;

public class Item2_Updater extends Updater<Item2, Item2_Updater> {
  final Item2_Schema schema;

  public Item2_Updater(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Updater(Item2_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Updater category1(@NonNull Category category1) {
    contents.put("`category1`", category1.id);
    return this;
  }

  public Item2_Updater category2(@Nullable Category category2) {
    contents.put("`category2`", category2.id);
    return this;
  }

  public Item2_Updater name(@NonNull String name) {
    contents.put("`name`", name);
    return this;
  }

  public Item2_Updater category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id /* primary key */);
  }

  public Item2_Updater category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Updater category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_Updater category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_Updater category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id /* primary key */);
  }

  public Item2_Updater category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  public Item2_Updater nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_Updater nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_Updater nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_Updater nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_Updater nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_Updater nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_Updater nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_Updater nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_Updater nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_Updater nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
