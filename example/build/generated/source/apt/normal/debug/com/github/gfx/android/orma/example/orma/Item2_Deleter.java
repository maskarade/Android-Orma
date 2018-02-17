package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.function.Function1;
import com.github.gfx.android.orma.rx.RxDeleter;
import com.github.gfx.android.orma.rx.RxOrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Item2_Deleter extends RxDeleter<Item2, Item2_Deleter> {
  final Item2_Schema schema;

  public Item2_Deleter(RxOrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Deleter(Item2_Deleter that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item2_Deleter(Item2_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item2_Deleter clone() {
    return new Item2_Deleter(this);
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Deleter category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id);
  }

  public Item2_Deleter category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Deleter category1(@NonNull Function1<Category_AssociationCondition, Category_AssociationCondition> block) {
    return block.apply(new Category_AssociationCondition(getConnection(), schema.category1.associationSchema)).appendTo(this);
  }

  public Item2_Deleter category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_Deleter category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_Deleter category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id);
  }

  public Item2_Deleter category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  public Item2_Deleter category2(@NonNull Function1<Category_AssociationCondition, Category_AssociationCondition> block) {
    return block.apply(new Category_AssociationCondition(getConnection(), schema.category2.associationSchema)).appendTo(this);
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
