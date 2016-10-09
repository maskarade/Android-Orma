package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Item2_Deleter extends Deleter<Item2, Item2_Deleter> {
  final Item2_Schema schema;

  public Item2_Deleter(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Deleter(Item2_Relation relation) {
    super(relation);
    this.schema = (Item2_Schema) relation.getSchema();
  }

  @Override
  @NonNull
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Deleter category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id /* primary key */);
  }

  public Item2_Deleter category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Deleter category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id /* primary key */);
  }

  public Item2_Deleter category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  public Item2_Deleter nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_Deleter nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_Deleter nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_Deleter nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_Deleter nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_Deleter nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_Deleter nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_Deleter nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_Deleter nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_Deleter nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
